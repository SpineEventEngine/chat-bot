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
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.net.Urls.urlOfSpec;

@DisplayName("OrganizationAggregate should")
final class OrganizationAggregateTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return GitHubContext.newBuilder();
    }

    @Test
    @DisplayName("register an organization")
    void register() {
        var id = OrganizationId
                .newBuilder()
                .setValue("TestOrganization")
                .vBuild();
        var registerOrganization = RegisterOrganization
                .newBuilder()
                .setId(id)
                .setGithubUrl(urlOfSpec("https://github.com/TestOrganization"))
                .setTravisCiUrl(urlOfSpec("https://travis-ci.com/TestOrganization"))
                .setWebsiteUrl(urlOfSpec("https://test-organization.com"))
                .setName("Test Organization")
                .vBuild();
        context().receivesCommand(registerOrganization);

        var expectedState = Organization
                .newBuilder()
                .setId(id)
                .setGithubUrl(urlOfSpec("https://github.com/TestOrganization"))
                .setTravisCiUrl(urlOfSpec("https://travis-ci.com/TestOrganization"))
                .setWebsiteUrl(urlOfSpec("https://test-organization.com"))
                .setName("Test Organization")
                .vBuild();
        context().assertState(id, Organization.class)
                 .isEqualTo(expectedState);
    }
}
