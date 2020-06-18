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
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.server.github.Identifiers.organizationIdOf;
import static io.spine.net.Urls.urlOfSpec;

@DisplayName("OrganizationAggregate should")
final class OrganizationAggregateTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return GitHubContext.newBuilder();
    }

    @Nested
    @DisplayName("register an organization")
    final class Register {

        private final OrganizationId organizationId = organizationIdOf("TestOrganization");

        private final Url githubUrl = urlOfSpec("https://github.com/TestOrganization");
        private final Url travisCiUrl = urlOfSpec("https://travis-ci.com/TestOrganization");
        private final Url websiteUrl = urlOfSpec("https://test-organization.com");
        private final String orgName = "Test Organization";
        private final String googleChatSpace = "spaces/qwdp123ttQ";

        @BeforeEach
        void setUp() {
            var registerOrganization = RegisterOrganization
                    .newBuilder()
                    .setId(organizationId)
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
                    .setId(organizationId)
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
                    .setId(organizationId)
                    .setGithubUrl(githubUrl)
                    .setTravisCiUrl(travisCiUrl)
                    .setWebsiteUrl(websiteUrl)
                    .setName(orgName)
                    .setGoogleChatSpace(googleChatSpace)
                    .vBuild();
            context().assertState(organizationId, Organization.class)
                     .isEqualTo(expectedState);
        }
    }
}
