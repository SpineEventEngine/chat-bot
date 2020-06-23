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

package io.spine.chatbot.server.github;

import io.spine.chatbot.api.travis.RepositoriesResponse;
import io.spine.chatbot.api.travis.Repository;
import io.spine.chatbot.github.organization.init.OrganizationInit;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.event.SpaceRegistered;
import io.spine.chatbot.server.google.chat.GoogleChatIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.server.github.SpineOrgInitProcess.SPINE_ORGANIZATION;

@DisplayName("SpineOrgInitProcess should")
final class SpineOrgInitProcessTest extends GitHubContextAwareTest {

    @Nested
    @DisplayName("perform initialization of watched spine resources")
    final class Init {

        private final SpaceId spaceId = GoogleChatIdentifier.space("spaces/qjwrp1441");
        private final Repository repository = Repository
                .newBuilder()
                .setId(123312L)
                .setName("time")
                .setSlug("SpineEventEngine/time")
                .vBuild();

        @BeforeEach
        void setUp() {
            var repositoriesResponse = RepositoriesResponse
                    .newBuilder()
                    .addRepositories(repository)
                    .vBuild();
            travisClient().setRepositoriesFor(SPINE_ORGANIZATION.getValue(), repositoriesResponse);
            var spaceRegistered = SpaceRegistered
                    .newBuilder()
                    .setId(spaceId)
                    .setDisplayName("Test space")
                    .setThreaded(true)
                    .vBuild();
            context().receivesExternalEvent(spaceRegistered);
        }

        @Test
        @DisplayName("setting process state")
        void settingState() {
            var expectedState = OrganizationInit
                    .newBuilder()
                    .setGoogleChatSpace(spaceId.getValue())
                    .setInitialized(true)
                    .setId(SPINE_ORGANIZATION)
                    .vBuild();
            context().assertState(SPINE_ORGANIZATION, OrganizationInit.class)
                     .isEqualTo(expectedState);
        }

        @Test
        @DisplayName("producing commands to register organization and repositories")
        void producingCommands() {
            context().assertCommands()
                     .hasSize(2);
        }
    }
}
