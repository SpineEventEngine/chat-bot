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
import io.spine.chatbot.github.Slugs;
import io.spine.chatbot.github.organization.OrgHeader;
import io.spine.chatbot.github.organization.OrganizationRepositories;
import io.spine.chatbot.github.organization.command.RegisterOrganization;
import io.spine.chatbot.github.repository.RepoHeader;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.net.Url;
import io.spine.net.Urls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.github.GitHubIdentifiers.organization;
import static io.spine.chatbot.github.GitHubIdentifiers.repository;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.space;
import static io.spine.chatbot.net.MoreUrls.githubUrlFor;
import static io.spine.chatbot.net.MoreUrls.travisUrlFor;

@DisplayName("`OrgReposProjection` should")
final class OrgReposProjectionTest extends GitHubContextAwareTest {

    @Nested
    @DisplayName("register an organization")
    final class RegisterOrg {

        private static final String orgName = "Our Org";

        private final SpaceId googleChatSpace = space("spaces/qwdp123tt1");
        private final OrganizationId organization = organization("OurOrg");

        private final Url githubUrl = githubUrlFor(Slugs.forOrg(organization));
        private final Url travisCiUrl = travisUrlFor(Slugs.forOrg(organization));
        private final Url websiteUrl = Urls.create("https://our-organization.com");

        private final OrgHeader header = OrgHeader
                .newBuilder()
                .setGithubProfile(githubUrl)
                .setTravisProfile(travisCiUrl)
                .setWebsite(websiteUrl)
                .setName(orgName)
                .setSpace(googleChatSpace)
                .vBuild();

        @BeforeEach
        void registerOrg() {
            var registerOrganization = RegisterOrganization
                    .newBuilder()
                    .setId(organization)
                    .setHeader(header)
                    .vBuild();
            context().receivesCommand(registerOrganization);
        }

        @Test
        @DisplayName("setting organization to the state")
        void settingState() {
            var expectedState = OrganizationRepositories
                    .newBuilder()
                    .setOrganization(organization)
                    .vBuild();
            context().assertState(organization, OrganizationRepositories.class)
                     .isEqualTo(expectedState);
        }
    }

    @Nested
    @DisplayName("register repositories")
    final class RegisterRepo {

        private static final String orgName = "Multi Repo Org";

        private final SpaceId space = space("spaces/qqwp123ttQ");
        private final OrganizationId org = organization("MultiRepoOrg");
        private final RepositoryId repo = repository("main-repo");

        private final Url githubUrl = githubUrlFor(Slugs.forOrg(org));
        private final Url travisCiUrl = travisUrlFor(Slugs.forOrg(org));
        private final Url websiteUrl = Urls.create("https://multi-repo-organization.com");

        private final OrgHeader orgHeader = OrgHeader
                .newBuilder()
                .setGithubProfile(githubUrl)
                .setTravisProfile(travisCiUrl)
                .setWebsite(websiteUrl)
                .setName(orgName)
                .setSpace(space)
                .vBuild();

        private final RepoHeader repoHeader = RepoHeader
                .newBuilder()
                .setGithubProfile(githubUrl)
                .setTravisProfile(travisCiUrl)
                .setName("Main Repo")
                .setOrganization(org)
                .vBuild();

        @BeforeEach
        void registerOrg() {
            var registerOrganization = RegisterOrganization
                    .newBuilder()
                    .setId(org)
                    .setHeader(orgHeader)
                    .vBuild();
            context().receivesCommand(registerOrganization);
        }

        @Test
        @DisplayName("setting repository to the state")
        void settingState() {
            var registerRepository = RegisterRepository
                    .newBuilder()
                    .setId(repo)
                    .setHeader(repoHeader)
                    .vBuild();
            context().receivesCommand(registerRepository);
            var expectedState = OrganizationRepositories
                    .newBuilder()
                    .setOrganization(org)
                    .addRepository(repo)
                    .vBuild();
            context().assertState(org, OrganizationRepositories.class)
                     .isEqualTo(expectedState);
        }

        @Test
        @DisplayName("handling duplicate repos gracefully")
        void handleDuplicateRepos() {
            var registerRepository = RegisterRepository
                    .newBuilder()
                    .setId(repo)
                    .setHeader(repoHeader)
                    .vBuild();
            context().receivesCommand(registerRepository);
            context().receivesCommand(registerRepository);
            var expectedState = OrganizationRepositories
                    .newBuilder()
                    .setOrganization(org)
                    .addRepository(repo)
                    .vBuild();
            context().assertState(org, OrganizationRepositories.class)
                     .isEqualTo(expectedState);
        }

        @Test
        @DisplayName("ignoring repos without organization")
        void ignoreRepoWithoutOrg() {
            var registerRepository = RegisterRepository
                    .newBuilder()
                    .setId(repo)
                    .setHeader(repoHeader.toBuilder()
                                         .clearOrganization())
                    .vBuild();
            context().receivesCommand(registerRepository);
            var expectedState = OrganizationRepositories
                    .newBuilder()
                    .setOrganization(org)
                    .vBuild();
            context().assertState(org, OrganizationRepositories.class)
                     .isEqualTo(expectedState);
        }
    }
}
