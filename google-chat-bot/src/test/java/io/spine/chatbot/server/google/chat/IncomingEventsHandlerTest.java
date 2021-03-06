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

package io.spine.chatbot.server.google.chat;

import io.spine.chatbot.google.chat.MessageId;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.incoming.ChatEvent;
import io.spine.chatbot.google.chat.incoming.EventType;
import io.spine.chatbot.google.chat.incoming.Message;
import io.spine.chatbot.google.chat.incoming.User;
import io.spine.chatbot.google.chat.incoming.event.BotAddedToSpace;
import io.spine.chatbot.google.chat.incoming.event.BotRemovedFromSpace;
import io.spine.chatbot.google.chat.incoming.event.ChatEventReceived;
import io.spine.chatbot.google.chat.incoming.event.MessageReceived;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.message;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.space;
import static io.spine.chatbot.google.chat.incoming.EventType.ADDED_TO_SPACE;
import static io.spine.chatbot.google.chat.incoming.EventType.MESSAGE;
import static io.spine.chatbot.google.chat.incoming.EventType.REMOVED_FROM_SPACE;
import static io.spine.chatbot.google.chat.incoming.SpaceType.ROOM;

@DisplayName("`IncomingEventsHandler` should")
final class IncomingEventsHandlerTest extends GoogleChatContextAwareTest {

    private static final SpaceId space = space("spaces/fqeq325661a");
    private static final MessageId message = message("spaces/fqeq325661a/messages/422");

    @Test
    @DisplayName("add bot to a space")
    void addBot() {
        // given
        var chatEventReceived = chatEventReceived(ADDED_TO_SPACE);
        var botAddedToSpace = BotAddedToSpace
                .newBuilder()
                .setEvent(chatEventReceived.getEvent())
                .setSpace(space)
                .vBuild();
        // when
        context().receivesExternalEvent(chatEventReceived);
        // then
        context().assertEvent(botAddedToSpace);
    }

    @Test
    @DisplayName("remove bot from the space")
    void removeBot() {
        // given
        var chatEventReceived = chatEventReceived(REMOVED_FROM_SPACE);
        var botRemovedFromSpace = BotRemovedFromSpace
                .newBuilder()
                .setEvent(chatEventReceived.getEvent())
                .setSpace(space)
                .vBuild();
        // when
        context().receivesExternalEvent(chatEventReceived);
        // then
        context().assertEvent(botRemovedFromSpace);
    }

    @Test
    @DisplayName("receive incoming message")
    void receiveIncomingMessage() {
        // given
        var chatEventReceived = chatEventReceived(MESSAGE);
        var messageReceived = MessageReceived
                .newBuilder()
                .setEvent(chatEventReceived.getEvent())
                .setMessage(message)
                .vBuild();
        // when
        context().receivesExternalEvent(chatEventReceived);
        // then
        context().assertEvent(messageReceived);
    }

    private static ChatEventReceived chatEventReceived(EventType type) {
        return ChatEventReceived
                .newBuilder()
                .setEvent(chatEventOfType(type))
                .vBuild();
    }

    private static ChatEvent chatEventOfType(EventType type) {
        return ChatEvent
                .newBuilder()
                .setSpace(chatSpace())
                .setType(type)
                .setUser(sender())
                .setEventTime("2020-06-20T15:42:02Z")
                .setMessage(chatMessage())
                .vBuild();
    }

    private static Message chatMessage() {
        return Message
                .newBuilder()
                .setName(message.getValue())
                .setSender(sender())
                .setText("To be, or not to be, that is the question.")
                .vBuild();
    }

    private static User sender() {
        return User
                .newBuilder()
                .setName("users/00qwe123")
                .vBuild();
    }

    private static io.spine.chatbot.google.chat.incoming.Space chatSpace() {
        return io.spine.chatbot.google.chat.incoming.Space
                .newBuilder()
                .setName(space.getValue())
                .setType(ROOM)
                .vBuild();
    }
}
