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
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SpaceAggregate should")
final class SpaceAggregateTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return GoogleChatContext.newBuilder();
    }

    @Test
    @DisplayName("register a space")
    void register() {
        var id = SpaceId
                .newBuilder()
                .setValue("spaces/qwroi12h3")
                .vBuild();
        var registerOrganization = RegisterSpace
                .newBuilder()
                .setId(id)
                .setName(id.getValue())
                .setThreaded(true)
                .setDisplayName("Spine Developers")
                .vBuild();
        context().receivesCommand(registerOrganization);

        var expectedState = Space
                .newBuilder()
                .setId(id)
                .setName(id.getValue())
                .setThreaded(true)
                .setDisplayName("Spine Developers")
                .vBuild();
        context().assertState(id, Space.class)
                 .isEqualTo(expectedState);
    }
}
