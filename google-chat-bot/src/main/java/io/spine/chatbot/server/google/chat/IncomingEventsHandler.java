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
import io.spine.core.External;
import io.spine.logging.Logging;
import io.spine.server.event.AbstractEventReactor;
import io.spine.server.event.React;
import io.spine.server.model.Nothing;
import io.spine.server.tuple.EitherOf4;

import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.spaceIdOf;

/**
 * Google Chat incoming events reactor.
 *
 * <p>Processes {@link ChatEvent}s received by the ChatBot.
 */
final class IncomingEventsHandler extends AbstractEventReactor implements Logging {

    /**
     * Processes incoming {@link ChatEvent} message and emits one of the following domain events:
     *
     * <ul>
     *     <li>{@link BotAddedToSpace} — the ChatBot is added to a Google Chat space;
     *     <li>{@link BotRemovedFromSpace} — the ChatBot is removed from the Google Chat space;
     *     <li>{@link MessageReceived} — the ChatBot received a new incoming message from a user
     *     within a Google Chat space.
     * </ul>
     *
     * <p>If the bot receives a chat event with a not supported currently event type,
     * {@link Nothing} is emitted.
     */
    @React
    EitherOf4<BotAddedToSpace, BotRemovedFromSpace, MessageReceived, Nothing>
    on(@External ChatEvent chatEvent) {
        switch (chatEvent.getType()) {
            case MESSAGE:
                _info().log("New user message received.");
                var message = chatEvent.getMessage();
                var messageId = GoogleChatIdentifier.messageIdOf(message.getName());
                var messageReceived = MessageReceived
                        .newBuilder()
                        .setEvent(chatEvent)
                        .setMessageId(messageId)
                        .vBuild();
                return EitherOf4.withC(messageReceived);
            case ADDED_TO_SPACE:
                var toSpace = chatEvent.getSpace();
                _info().log("ChatBot added to space `%s` (%s).",
                            toSpace.getDisplayName(), toSpace.getName());
                var addedToSpace = BotAddedToSpace
                        .newBuilder()
                        .setEvent(chatEvent)
                        .setSpaceId(spaceIdOf(toSpace.getName()))
                        .vBuild();
                return EitherOf4.withA(addedToSpace);
            case REMOVED_FROM_SPACE:
                var fromSpace = chatEvent.getSpace();
                _info().log("ChatBot removed from space `%s` (%s).",
                            fromSpace.getDisplayName(), fromSpace.getName());
                var removedFromSpace = BotRemovedFromSpace
                        .newBuilder()
                        .setEvent(chatEvent)
                        .setSpaceId(spaceIdOf(fromSpace.getName()))
                        .vBuild();
                return EitherOf4.withB(removedFromSpace);

            case CARD_CLICKED:
            case UNRECOGNIZED:
            case ET_UNKNOWN:
            default:
                _debug().log("Unsupported chat event type received: %s", chatEvent.getType());
                return EitherOf4.withD(nothing());
        }
    }
}
