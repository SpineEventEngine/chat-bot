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

import io.spine.chatbot.github.OrganizationId;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.repository.Repository;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.chatbot.github.repository.event.RepositoryRegistered;
import io.spine.net.Url;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.server.github.GitHubIdentifier.organization;
import static io.spine.chatbot.server.github.GitHubIdentifier.repository;
import static io.spine.net.Urls.urlOfSpec;

@DisplayName("RepositoryAggregate should")
final class RepositoryAggregateTest extends GitHubContextAwareTest {

    @Nested
    @DisplayName("register a repository")
    final class Register {

        private final RepositoryId repositoryId = repository("SpineEventEngine/base");
        private final OrganizationId organizationId = organization("SpineEventEngine");

        private final Url githubUrl = urlOfSpec("https://github.com/SpineEventEngine/base");
        private final Url travisCiUrl =
                urlOfSpec("https://travis-ci.com/github/SpineEventEngine/base");
        private final String repositoryName = "Spine Base";

        @BeforeEach
        void setUp() {
            var registerRepository = RegisterRepository
                    .newBuilder()
                    .setId(repositoryId)
                    .setGithubUrl(githubUrl)
                    .setTravisCiUrl(travisCiUrl)
                    .setName(repositoryName)
                    .setOrganization(organizationId)
                    .vBuild();
            context().receivesCommand(registerRepository);
        }

        @Test
        @DisplayName("producing RepositoryRegistered event")
        void producingEvent() {
            var repositoryRegistered = RepositoryRegistered
                    .newBuilder()
                    .setId(repositoryId)
                    .setGithubUrl(githubUrl)
                    .setTravisCiUrl(travisCiUrl)
                    .setName(repositoryName)
                    .setOrganization(organizationId)
                    .vBuild();
            context().assertEvent(repositoryRegistered);
        }

        @Test
        @DisplayName("setting repository state")
        void settingState() {
            var expectedState = Repository
                    .newBuilder()
                    .setId(repositoryId)
                    .setGithubUrl(githubUrl)
                    .setTravisCiUrl(travisCiUrl)
                    .setName(repositoryName)
                    .setOrganization(organizationId)
                    .vBuild();
            context().assertState(repositoryId, Repository.class)
                     .isEqualTo(expectedState);
        }
    }
}
