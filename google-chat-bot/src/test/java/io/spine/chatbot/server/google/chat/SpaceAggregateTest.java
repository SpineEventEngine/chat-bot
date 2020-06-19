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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SpaceAggregate should")
final class SpaceAggregateTest extends GoogleChatEntityTest {

    @Nested
    @DisplayName("register a space")
    final class Register {

        private final SpaceId spaceId = Identifiers.spaceIdOf("spaces/poqwdpQ21");
        private final String displayName = "Spine Developers";

        @BeforeEach
        void setUp() {
            var registerSpace = RegisterSpace
                    .newBuilder()
                    .setId(spaceId)
                    .setThreaded(true)
                    .setDisplayName(displayName)
                    .vBuild();
            context().receivesCommand(registerSpace);
        }

        @Test
        @DisplayName("producing SpaceRegistered event")
        void producingEvent() {
            var spaceRegistered = SpaceRegistered
                    .newBuilder()
                    .setId(spaceId)
                    .setDisplayName(displayName)
                    .setThreaded(true)
                    .vBuild();
            context().assertEvent(spaceRegistered);
        }

        @Test
        @DisplayName("setting Space state")
        void settingState() {
            var expectedState = Space
                    .newBuilder()
                    .setId(spaceId)
                    .setThreaded(true)
                    .setDisplayName(displayName)
                    .vBuild();
            context().assertState(spaceId, Space.class)
                     .isEqualTo(expectedState);
        }
    }
}
