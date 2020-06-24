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
import io.spine.chatbot.github.organization.Organization;
import io.spine.chatbot.github.organization.command.RegisterOrganization;
import io.spine.chatbot.github.organization.event.OrganizationRegistered;
import io.spine.net.Url;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.server.github.GitHubIdentifier.organization;
import static io.spine.net.Urls.githubUrlFor;
import static io.spine.net.Urls.travisUrlFor;
import static io.spine.net.Urls.urlOfSpec;

@DisplayName("OrganizationAggregate should")
final class OrganizationAggregateTest extends GitHubContextAwareTest {

    @Nested
    @DisplayName("register an organization")
    final class Register {

        private static final String orgName = "Test Organization";
        private static final String googleChatSpace = "spaces/qwdp123ttQ";

        private final OrganizationId organization = organization("TestOrganization");

        private final Url githubUrl = githubUrlFor(organization.getValue());
        private final Url travisCiUrl = travisUrlFor(organization.getValue());
        private final Url websiteUrl = urlOfSpec("https://test-organization.com");

        @BeforeEach
        void setUp() {
            var registerOrganization = RegisterOrganization
                    .newBuilder()
                    .setId(organization)
                    .setGithubUrl(githubUrl)
                    .setTravisCiUrl(travisCiUrl)
                    .setWebsiteUrl(websiteUrl)
                    .setName(orgName)
                    .setGoogleChatSpace(googleChatSpace)
                    .vBuild();
            context().receivesCommand(registerOrganization);
        }

        @Test
        @DisplayName("producing OrganizationRegistered event")
        void producingEvent() {
            var organizationRegistered = OrganizationRegistered
                    .newBuilder()
                    .setOrganization(organization)
                    .setGithubUrl(githubUrl)
                    .setTravisCiUrl(travisCiUrl)
                    .setWebsiteUrl(websiteUrl)
                    .setName(orgName)
                    .setGoogleChatSpace(googleChatSpace)
                    .vBuild();
            context().assertEvent(organizationRegistered);
        }

        @Test
        @DisplayName("setting organization state")
        void settingState() {
            var expectedState = Organization
                    .newBuilder()
                    .setId(organization)
                    .setGithubUrl(githubUrl)
                    .setTravisCiUrl(travisCiUrl)
                    .setWebsiteUrl(websiteUrl)
                    .setName(orgName)
                    .setGoogleChatSpace(googleChatSpace)
                    .vBuild();
            context().assertState(organization, Organization.class)
                     .isEqualTo(expectedState);
        }
    }
}
