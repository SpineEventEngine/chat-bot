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

import io.spine.chatbot.google.chat.Space;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.command.RegisterSpace;
import io.spine.chatbot.google.chat.event.SpaceRegistered;
import io.spine.chatbot.google.chat.incoming.ChatEvent;
import io.spine.chatbot.google.chat.incoming.EventType;
import io.spine.chatbot.google.chat.incoming.SpaceType;
import io.spine.chatbot.google.chat.incoming.User;
import io.spine.chatbot.google.chat.incoming.event.BotAddedToSpace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.space;

@DisplayName("SpaceAggregate should")
final class SpaceAggregateTest extends GoogleChatContextAwareTest {

    private static final SpaceId SPACE = space("spaces/poqwdpQ21");
    private static final String DISPLAY_NAME = "Spine Developers";

    @Nested
    @DisplayName("register a space")
    final class Register {

        @BeforeEach
        void setUp() {
            var registerSpace = RegisterSpace
                    .newBuilder()
                    .setSpace(SPACE)
                    .setThreaded(true)
                    .setDisplayName(DISPLAY_NAME)
                    .vBuild();
            context().receivesCommand(registerSpace);
        }

        @Test
        @DisplayName("producing SpaceRegistered event")
        void producingEvent() {
            var spaceRegistered = SpaceRegistered
                    .newBuilder()
                    .setSpace(SPACE)
                    .setDisplayName(DISPLAY_NAME)
                    .setThreaded(true)
                    .vBuild();
            context().assertEvent(spaceRegistered);
        }

        @Test
        @DisplayName("setting Space state")
        void settingState() {
            var expectedState = Space
                    .newBuilder()
                    .setSpace(SPACE)
                    .setThreaded(true)
                    .setDisplayName(DISPLAY_NAME)
                    .vBuild();
            context().assertState(SPACE, Space.class)
                     .isEqualTo(expectedState);
        }
    }

    @Nested
    @DisplayName("register a space when a bot is added to the space")
    final class RegisterOnAddedBot {

        @BeforeEach
        void setUp() {
            var chatEvent = ChatEvent
                    .newBuilder()
                    .setSpace(chatSpace())
                    .setUser(User.newBuilder()
                                 .setName("users/12e1ojep1"))
                    .setType(EventType.ADDED_TO_SPACE)
                    .setEventTime("2020-06-19T15:39:01Z")
                    .vBuild();
            var botAddedToSpace = BotAddedToSpace
                    .newBuilder()
                    .setSpace(SPACE)
                    .setEvent(chatEvent)
                    .vBuild();
            context().receivesEvent(botAddedToSpace);
        }

        @Test
        @DisplayName("producing SpaceRegistered event")
        void producingEvent() {
            var spaceRegistered = SpaceRegistered
                    .newBuilder()
                    .setSpace(SPACE)
                    .setDisplayName(DISPLAY_NAME)
                    .setThreaded(true)
                    .vBuild();
            context().assertEvent(spaceRegistered);
        }

        @Test
        @DisplayName("setting Space state")
        void settingState() {
            var expectedState = Space
                    .newBuilder()
                    .setSpace(SPACE)
                    .setThreaded(true)
                    .setDisplayName(DISPLAY_NAME)
                    .vBuild();
            context().assertState(SPACE, Space.class)
                     .isEqualTo(expectedState);
        }

        private io.spine.chatbot.google.chat.incoming.Space chatSpace() {
            return io.spine.chatbot.google.chat.incoming.Space
                    .newBuilder()
                    .setName(SPACE.getValue())
                    .setDisplayName(DISPLAY_NAME)
                    .setType(SpaceType.ROOM)
                    .vBuild();
        }
    }
}
