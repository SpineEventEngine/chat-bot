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

package io.spine.chatbot.api.google.chat;

import com.google.api.services.chat.v1.HangoutsChat;
import com.google.api.services.chat.v1.model.Message;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.chatbot.github.repository.build.Build;
import io.spine.chatbot.google.chat.BuildStateUpdate;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.thread.ThreadResource;
import io.spine.logging.Logging;

import java.io.IOException;

import static io.spine.chatbot.api.google.chat.BuildStateUpdates.buildStateMessage;
import static io.spine.chatbot.api.google.chat.HangoutsChatProvider.newHangoutsChat;
import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.message;
import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.thread;
import static io.spine.chatbot.server.google.chat.ThreadResources.threadResource;

/**
 * Google Chat API client.
 *
 * @see <a href="https://developers.google.com/hangouts/chat/concepts">Google Chat API</a>
 */
public final class GoogleChat implements GoogleChatClient, Logging {

    private final HangoutsChat chat;

    private GoogleChat(HangoutsChat chat) {
        this.chat = chat;
    }

    /**
     * Creates a new Google Chat client.
     *
     * <p>The client is backed by {@link HangoutsChat} API.
     */
    public static GoogleChatClient newInstance() {
        return new GoogleChat(newHangoutsChat());
    }

    @Override
    public BuildStateUpdate sendBuildStateUpdate(Build build, ThreadResource thread) {
        var repo = build.getRepository();
        var debug = _debug();
        debug.log("Building state update message for repository `%s`.", repo);
        var message = buildStateMessage(build, thread);
        debug.log("Sending state update message for repository `%s`.", repo);
        var sentMessage = sendMessage(build.getSpace(), message);
        debug.log(
                "Build state update message with ID `%s` for repository `%s` sent to thread `%s`.",
                sentMessage.getName(), repo, sentMessage.getThread()
                                                        .getName()
        );
        return BuildStateUpdate
                .newBuilder()
                .setMessage(message(message.getName()))
                .setResource(threadResource(message.getThread()
                                                   .getName()))
                .setSpace(build.getSpace())
                .setThread(thread(repo.value()))
                .vBuild();
    }

    @CanIgnoreReturnValue
    private Message sendMessage(SpaceId space, Message message) {
        try {
            return chat
                    .spaces()
                    .messages()
                    .create(space.getValue(), message)
                    .execute();
        } catch (IOException e) {
            _error().withCause(e)
                    .log("Unable to send message to space `%s`.", space);
            throw new RuntimeException("Unable to send message to space " + space, e);
        }
    }
}
