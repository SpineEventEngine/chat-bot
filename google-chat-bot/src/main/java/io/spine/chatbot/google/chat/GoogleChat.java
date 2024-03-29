/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.chatbot.google.chat;

import com.google.api.services.chat.v1.HangoutsChat;
import com.google.api.services.chat.v1.model.Message;
import io.spine.chatbot.github.repository.build.Build;
import io.spine.chatbot.google.chat.thread.ThreadResource;
import io.spine.logging.Logging;

import java.io.IOException;

import static com.google.api.client.util.Preconditions.checkNotNull;
import static io.spine.chatbot.google.chat.BuildStateUpdates.buildStateMessage;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.message;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.thread;
import static io.spine.chatbot.server.google.chat.ThreadResources.threadResource;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Google Chat API client.
 *
 * @see <a href="https://developers.google.com/hangouts/chat/concepts">Google Chat API</a>
 */
record GoogleChat(HangoutsChat chat) implements GoogleChatClient, Logging {

    GoogleChat(HangoutsChat chat) {
        this.chat = checkNotNull(chat);
    }

    @Override
    public BuildStateUpdate sendBuildStateUpdate(Build build, ThreadResource thread) {
        checkNotNull(build);
        checkNotNull(thread);
        var repo = build.getRepository();
        _trace().log("Building state update message for the repository `%s`.", repo);
        var message = buildStateMessage(build, thread);
        _trace().log("Sending state update message for the repository `%s`.", repo);
        var sentMessage = sendMessage(build.getSpace(), message);
        var messageName = sentMessage.getName();
        var messageThread = sentMessage.getThread();
        _trace().log(
                "Build state update message with ID `%s` " +
                        "for the repository `%s` sent to the thread `%s`.",
                messageName, repo, messageThread.getName()
        );
        return BuildStateUpdate
                .newBuilder()
                .setMessage(message(messageName))
                .setResource(threadResource(messageThread.getName()))
                .setSpace(build.getSpace())
                .setThread(thread(repo.value()))
                .vBuild();
    }

    private Message sendMessage(SpaceId space, Message message) {
        try {
            return chat
                    .spaces()
                    .messages()
                    .create(space.getValue(), message)
                    .execute();
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Unable to send message to the space `%s`.", space
            );
        }
    }
}
