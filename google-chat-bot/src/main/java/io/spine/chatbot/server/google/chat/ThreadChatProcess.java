/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.chatbot.server.google.chat;

import com.google.errorprone.annotations.concurrent.LazyInit;
import io.spine.chatbot.api.google.chat.GoogleChatClient;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.repository.build.BuildState;
import io.spine.chatbot.github.repository.build.event.BuildFailed;
import io.spine.chatbot.github.repository.build.event.BuildRecovered;
import io.spine.chatbot.google.chat.ThreadId;
import io.spine.chatbot.google.chat.event.MessageCreated;
import io.spine.chatbot.google.chat.event.ThreadCreated;
import io.spine.chatbot.google.chat.thread.ThreadChat;
import io.spine.core.External;
import io.spine.logging.Logging;
import io.spine.protobuf.Messages;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;
import io.spine.server.tuple.Pair;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Optional;

import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.message;
import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.space;
import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.thread;
import static io.spine.chatbot.server.google.chat.ThreadResources.threadResource;

/**
 * A process of notifying thread members about the changes in the watched resouces.
 */
final class ThreadChatProcess extends ProcessManager<ThreadId, ThreadChat, ThreadChat.Builder>
        implements Logging {

    @LazyInit
    private @MonotonicNonNull GoogleChatClient client;

    /**
     * Notifies thread members about a failed CI build.
     */
    @React
    Pair<MessageCreated, Optional<ThreadCreated>> on(@External BuildFailed e) {
        var change = e.getChange();
        var buildState = change.getNewValue();
        var repository = e.getRepository();
        _info().log("Build for repository `%s` failed.", repository.getValue());
        return processBuildStateUpdate(buildState, repository);
    }

    /**
     * Notifies thread members about a recovered CI build.
     *
     * <p>The build is considered a recovered when it changes its state from
     * {@code failed} to {@code passing}.
     */
    @React
    Pair<MessageCreated, Optional<ThreadCreated>> on(@External BuildRecovered e) {
        var change = e.getChange();
        var buildState = change.getNewValue();
        var repository = e.getRepository();
        _info().log("Build for repository `%s` recovered.", repository.getValue());
        return processBuildStateUpdate(buildState, repository);
    }

    private Pair<MessageCreated, Optional<ThreadCreated>>
    processBuildStateUpdate(BuildState buildState, RepositoryId repository) {
        var sentMessage = client.sendBuildStateUpdate(buildState, state().getResource());
        var message = message(sentMessage.getName());
        var thread = thread(repository.getValue());
        var space = space(buildState.getGoogleChatSpace());
        var messageCreated = MessageCreated
                .newBuilder()
                .setMessage(message)
                .setSpace(space)
                .setThread(thread)
                .vBuild();
        if (shouldCreateThread()) {
            var resource = threadResource(sentMessage.getThread()
                                                     .getName());
            _debug().log("New thread `%s` created for repository `%s`.",
                         resource.getName(), repository.getValue());
            builder().setResource(resource)
                     .setSpace(space);
            var threadCreated = ThreadCreated
                    .newBuilder()
                    .setThread(thread)
                    .setResource(resource)
                    .setSpace(space)
                    .vBuild();
            return Pair.withNullable(messageCreated, threadCreated);
        }
        return Pair.withNullable(messageCreated, null);
    }

    private boolean shouldCreateThread() {
        return Messages.isDefault(state().getResource());
    }

    /**
     * Sets {@link #client} to be used during handling of signals.
     *
     * @implNote the method is intended to be used as part of the entity configuration
     *         done through the repository
     */
    void setClient(GoogleChatClient client) {
        this.client = client;
    }
}
