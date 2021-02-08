/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import io.spine.chatbot.github.organization.OrgHeader;
import io.spine.chatbot.github.organization.Organization;
import io.spine.chatbot.github.organization.command.RegisterOrganization;
import io.spine.chatbot.github.organization.event.OrganizationRegistered;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.net.Url;
import io.spine.net.Urls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.github.GitHubIdentifiers.organization;
import static io.spine.chatbot.github.Slugs.orgSlug;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.space;
import static io.spine.chatbot.net.MoreUrls.githubUrlFor;
import static io.spine.chatbot.net.MoreUrls.travisUrlFor;

@DisplayName("`OrganizationAggregate` should")
final class OrganizationAggregateTest extends GitHubContextAwareTest {

    @Nested
    @DisplayName("register an organization")
    final class Register {

        private static final String orgName = "Test Organization";

        private final SpaceId googleChatSpace = space("spaces/qwdp123ttQ");
        private final OrganizationId organization = organization("TestOrganization");

        private final Url githubUrl = githubUrlFor(orgSlug(organization));
        private final Url travisCiUrl = travisUrlFor(orgSlug(organization));
        private final Url websiteUrl = Urls.create("https://test-organization.com");

        private final OrgHeader header = OrgHeader
                .newBuilder()
                .setGithubProfile(githubUrl)
                .setTravisProfile(travisCiUrl)
                .setWebsite(websiteUrl)
                .setName(orgName)
                .setSpace(googleChatSpace)
                .vBuild();

        @BeforeEach
        void registerOrganization() {
            var registerOrganization = RegisterOrganization
                    .newBuilder()
                    .setId(organization)
                    .setHeader(header)
                    .vBuild();
            context().receivesCommand(registerOrganization);
        }

        @Test
        @DisplayName("producing `OrganizationRegistered` event")
        void producingEvent() {
            var organizationRegistered = OrganizationRegistered
                    .newBuilder()
                    .setOrganization(organization)
                    .setHeader(header)
                    .vBuild();
            context().assertEvent(organizationRegistered);
        }

        @Test
        @DisplayName("setting organization state")
        void settingState() {
            var expectedState = Organization
                    .newBuilder()
                    .setId(organization)
                    .setHeader(header)
                    .vBuild();
            context().assertState(organization, Organization.class)
                     .isEqualTo(expectedState);
        }
    }
}
