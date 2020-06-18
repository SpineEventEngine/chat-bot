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

import com.google.common.base.Strings;
import io.spine.chatbot.api.GoogleChatClient;
import io.spine.chatbot.github.repository.build.event.BuildStateChanged;
import io.spine.chatbot.google.chat.ThreadId;
import io.spine.chatbot.google.chat.event.MessageCreated;
import io.spine.chatbot.google.chat.event.ThreadCreated;
import io.spine.chatbot.google.chat.thread.ThreadChat;
import io.spine.core.External;
import io.spine.logging.Logging;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;
import io.spine.server.tuple.Pair;

import java.util.Optional;

import static io.spine.chatbot.server.google.chat.Identifiers.messageIdOf;
import static io.spine.chatbot.server.google.chat.Identifiers.spaceIdOf;
import static io.spine.chatbot.server.google.chat.Identifiers.threadIdOf;
import static io.spine.chatbot.server.google.chat.ThreadResources.threadResourceOf;

final class ThreadChatProcess extends ProcessManager<ThreadId, ThreadChat, ThreadChat.Builder>
        implements Logging {

    @React
    Pair<MessageCreated, Optional<ThreadCreated>> on(@External BuildStateChanged e) {
        var change = e.getChange();
        var buildState = change.getNewValue();
        var repositoryId = e.getId();
        var threadId = threadIdOf(repositoryId.getValue());
        var spaceId = spaceIdOf(buildState.getGoogleChatSpace());
        var currentThread = state().getThread();
        _info().log("Build state changed for the repository `%s`.", repositoryId.getValue());
        var sentMessage = GoogleChatClient.sendBuildStateUpdate(buildState,
                                                                currentThread.getName());
        var thread = sentMessage.getThread();
        var messageId = messageIdOf(sentMessage.getName());
        var messageCreated = MessageCreated
                .newBuilder()
                .setId(messageId)
                .setSpaceId(spaceId)
                .setThreadId(threadId)
                .vBuild();
        if (Strings.isNullOrEmpty(currentThread.getName())) {
            var newThread = threadResourceOf(thread.getName());
            builder().setThread(newThread)
                     .setSpaceId(spaceId);
            var threadCreated = ThreadCreated
                    .newBuilder()
                    .setId(threadId)
                    .setThread(newThread)
                    .setSpaceId(spaceId)
                    .vBuild();
            return Pair.withNullable(messageCreated, threadCreated);
        }
        return Pair.withNullable(messageCreated, null);
    }
}
