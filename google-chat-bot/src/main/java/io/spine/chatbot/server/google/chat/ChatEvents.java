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

import io.spine.chatbot.google.chat.incoming.ChatEvent;
import io.spine.chatbot.google.chat.incoming.event.BotAddedToSpace;
import io.spine.chatbot.google.chat.incoming.event.BotRemovedFromSpace;
import io.spine.chatbot.google.chat.incoming.event.MessageReceived;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.message;

/**
 * A utility for working with {@link ChatEvent}s.
 */
final class ChatEvents {

    /**
     * Prevents instantiation of this utility class.
     */
    private ChatEvents() {
    }

    /**
     * Creates a new {@code BotRemovedFromSpace} message out of the supplied {@code event}.
     */
    static BotRemovedFromSpace toBotRemovedFromSpace(ChatEvent event) {
        checkNotNull(event);
        var space = event.getSpace();
        return BotRemovedFromSpace
                .newBuilder()
                .setEvent(event)
                .setSpace(space.id())
                .vBuild();
    }

    /**
     * Creates a new {@code BotAddedToSpace} message out of the supplied {@code event}.
     */
    static BotAddedToSpace toBotAddedToSpace(ChatEvent event) {
        checkNotNull(event);
        var space = event.getSpace();
        return BotAddedToSpace
                .newBuilder()
                .setEvent(event)
                .setSpace(space.id())
                .vBuild();
    }

    /**
     * Creates a new {@code MessageReceived} message out of the supplied {@code event}.
     */
    static MessageReceived toMessageReceived(ChatEvent event) {
        checkNotNull(event);
        var message = event.getMessage();
        return MessageReceived
                .newBuilder()
                .setEvent(event)
                .setMessage(message(message.getName()))
                .vBuild();
    }
}
