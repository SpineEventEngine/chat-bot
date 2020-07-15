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

import io.spine.chatbot.github.Slugs;
import io.spine.chatbot.github.organization.init.OrganizationInit;
import io.spine.chatbot.google.chat.SpaceHeader;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.event.SpaceRegistered;
import io.spine.chatbot.travis.RepositoriesResponse;
import io.spine.chatbot.travis.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.space;
import static io.spine.chatbot.server.github.SpineOrgInitProcess.ORGANIZATION;

@DisplayName("`SpineOrgInitProcess` should")
final class SpineOrgInitProcessTest extends GitHubContextAwareTest {

    @Nested
    @DisplayName("perform initialization of watched Spine resources")
    final class Init {

        private final SpaceId space = space("spaces/qjwrp1441");
        private final Repository repo = Repository
                .newBuilder()
                .setId(123312L)
                .setName("time")
                .setSlug("SpineEventEngine/time")
                .vBuild();
        private final SpaceHeader spaceHeader = SpaceHeader
                .newBuilder()
                .setThreaded(true)
                .setDisplayName("Test Space")
                .vBuild();

        @BeforeEach
        void registerSpace() {
            var repositoriesResponse = RepositoriesResponse
                    .newBuilder()
                    .addRepositories(repo)
                    .vBuild();
            travisClient().setRepositoriesFor(Slugs.forOrg(ORGANIZATION), repositoriesResponse);
            var spaceRegistered = SpaceRegistered
                    .newBuilder()
                    .setSpace(space)
                    .setHeader(spaceHeader)
                    .vBuild();
            context().receivesExternalEvent(spaceRegistered);
        }

        @Test
        @DisplayName("setting process state")
        void settingState() {
            var expectedState = OrganizationInit
                    .newBuilder()
                    .setSpace(space)
                    .setInitialized(true)
                    .setOrganization(ORGANIZATION)
                    .vBuild();
            context().assertState(ORGANIZATION, OrganizationInit.class)
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
