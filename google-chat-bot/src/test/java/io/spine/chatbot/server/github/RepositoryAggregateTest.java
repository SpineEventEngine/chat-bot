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
import io.spine.chatbot.github.repository.RepoHeader;
import io.spine.chatbot.github.repository.Repository;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.chatbot.github.repository.event.RepositoryRegistered;
import io.spine.net.Url;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.server.github.GitHubIdentifiers.organization;
import static io.spine.chatbot.server.github.GitHubIdentifiers.repository;
import static io.spine.chatbot.net.MoreUrls.githubUrlFor;
import static io.spine.chatbot.net.MoreUrls.travisUrlFor;

@DisplayName("`RepositoryAggregate` should")
final class RepositoryAggregateTest extends GitHubContextAwareTest {

    @Nested
    @DisplayName("register a repository")
    final class Register {

        private static final String ORG_SLUG = "SpineEventEngine";
        private static final String REPO_SLUG = ORG_SLUG + "/base";
        private static final String REPO_NAME = "Spine Base";

        private final RepositoryId repository = repository(REPO_SLUG);
        private final OrganizationId organization = organization(ORG_SLUG);

        private final Url githubUrl = githubUrlFor(Slugs.forOrg(organization));
        private final Url travisCiUrl = travisUrlFor(Slugs.forRepo(repository));
        private final RepoHeader header = RepoHeader
                .newBuilder()
                .setGithubProfile(githubUrl)
                .setTravisProfile(travisCiUrl)
                .setName(REPO_NAME)
                .setOrganization(organization)
                .vBuild();

        @BeforeEach
        void registerRepository() {
            var registerRepository = RegisterRepository
                    .newBuilder()
                    .setId(repository)
                    .setHeader(header)
                    .vBuild();
            context().receivesCommand(registerRepository);
        }

        @Test
        @DisplayName("producing RepositoryRegistered event")
        void producingEvent() {
            var repositoryRegistered = RepositoryRegistered
                    .newBuilder()
                    .setRepository(repository)
                    .setHeader(header)
                    .vBuild();
            context().assertEvent(repositoryRegistered);
        }

        @Test
        @DisplayName("setting repository state")
        void settingState() {
            var expectedState = Repository
                    .newBuilder()
                    .setId(repository)
                    .setHeader(header)
                    .vBuild();
            context().assertState(repository, Repository.class)
                     .isEqualTo(expectedState);
        }
    }
}
