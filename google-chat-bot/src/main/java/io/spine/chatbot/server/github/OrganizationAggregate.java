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
import io.spine.logging.Logging;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

final class OrganizationAggregate
        extends Aggregate<OrganizationId, Organization, Organization.Builder>
        implements Logging {

    @Assign
    OrganizationRegistered handle(RegisterOrganization c) {
        _info().log("Registering organization `%s`.", idAsString());
        return OrganizationRegistered
                .newBuilder()
                .setId(c.getId())
                .setGithubUrl(c.getGithubUrl())
                .setName(c.getName())
                .setTravisCiUrl(c.getTravisCiUrl())
                .setWebsiteUrl(c.getWebsiteUrl())
                .setGoogleChatSpace(c.getGoogleChatSpace())
                .vBuild();
    }

    @Apply
    private void on(OrganizationRegistered e) {
        builder().setName(e.getName())
                 .setGithubUrl(e.getGithubUrl())
                 .setTravisCiUrl(e.getTravisCiUrl())
                 .setWebsiteUrl(e.getWebsiteUrl())
                 .setGoogleChatSpace(e.getGoogleChatSpace());
    }
}
