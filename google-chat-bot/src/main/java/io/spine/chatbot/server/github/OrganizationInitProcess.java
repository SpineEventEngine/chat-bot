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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.spine.base.CommandMessage;
import io.spine.chatbot.github.OrganizationId;
import io.spine.chatbot.github.organization.command.RegisterOrganization;
import io.spine.chatbot.github.organization.init.OrganizationInit;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.chatbot.google.chat.event.SpaceRegistered;
import io.spine.chatbot.travis.Repository;
import io.spine.core.External;
import io.spine.logging.Logging;
import io.spine.server.command.Command;
import io.spine.server.procman.ProcessManager;

import static io.spine.chatbot.api.TravisClient.defaultTravisClient;
import static io.spine.chatbot.server.github.Identifiers.organizationIdOf;
import static io.spine.chatbot.server.github.Identifiers.repositoryIdOf;
import static io.spine.net.Urls.githubRepoUrlFor;
import static io.spine.net.Urls.travisRepoUrlFor;
import static io.spine.net.Urls.urlOfSpec;

final class OrganizationInitProcess
        extends ProcessManager<OrganizationId, OrganizationInit, OrganizationInit.Builder>
        implements Logging {

    private static final String SPINE_ORG = "SpineEventEngine";

    private static final ImmutableList<String> WATCHED_REPOS = ImmutableList.of(
            "base", "time", "core-java", "web", "gcloud-java", "bootstrap", "money", "jdbc-storage"
    );
    static final OrganizationId SPINE_ORGANIZATION = organizationIdOf(SPINE_ORG);

    @Command
    Iterable<CommandMessage> on(@External SpaceRegistered e) {
        if (state().getIsInitialized()) {
            return ImmutableSet.of();
        }
        var spaceId = e.getId();
        var commands = ImmutableSet.<CommandMessage>builder();
        commands.add(registerOrganizationCommand(SPINE_ORGANIZATION, spaceId.getValue()));
        defaultTravisClient()
                .queryRepositoriesFor(SPINE_ORG)
                .getRepositoriesList()
                .stream()
                .filter(repository -> WATCHED_REPOS.contains(repository.getName()))
                .map(repository -> registerRepoCommand(repository, SPINE_ORGANIZATION))
                .forEach(commands::add);
        var result = commands.build();
        return result;
    }

    private static RegisterRepository registerRepoCommand(Repository repository,
                                                          OrganizationId orgId) {
        var slug = repository.getSlug();
        return RegisterRepository
                .newBuilder()
                .setOrganization(orgId)
                .setGithubUrl(githubRepoUrlFor(slug))
                .setId(repositoryIdOf(slug))
                .setName(repository.getName())
                .setTravisCiUrl(travisRepoUrlFor(slug))
                .vBuild();
    }

    private RegisterOrganization registerOrganizationCommand(OrganizationId spineOrgId,
                                                             String spaceName) {
        _info().log("Registering `Spine Event Engine` organization.");
        return RegisterOrganization
                .newBuilder()
                .setName("Spine Event Engine")
                .setWebsiteUrl(urlOfSpec("https://spine.io/"))
                .setTravisCiUrl(urlOfSpec("https://travis-ci.com/github/SpineEventEngine"))
                .setGithubUrl(urlOfSpec("https://github.com/SpineEventEngine"))
                .setId(spineOrgId)
                .setGoogleChatSpace(spaceName)
                .vBuild();
    }
}
