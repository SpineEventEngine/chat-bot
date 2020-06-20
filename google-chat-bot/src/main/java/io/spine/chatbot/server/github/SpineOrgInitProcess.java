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
import com.google.errorprone.annotations.concurrent.LazyInit;
import io.spine.base.CommandMessage;
import io.spine.chatbot.api.travis.ReposQuery;
import io.spine.chatbot.api.travis.Repository;
import io.spine.chatbot.api.travis.TravisClient;
import io.spine.chatbot.github.OrganizationId;
import io.spine.chatbot.github.organization.command.RegisterOrganization;
import io.spine.chatbot.github.organization.init.OrganizationInit;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.chatbot.google.chat.event.SpaceRegistered;
import io.spine.core.External;
import io.spine.logging.Logging;
import io.spine.server.command.Command;
import io.spine.server.procman.ProcessManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static io.spine.chatbot.server.github.GitHubIdentifier.organizationIdOf;
import static io.spine.chatbot.server.github.GitHubIdentifier.repositoryIdOf;
import static io.spine.net.Urls.githubRepoUrlFor;
import static io.spine.net.Urls.travisRepoUrlFor;
import static io.spine.net.Urls.urlOfSpec;

/**
 * Spine Event Engine organization initialization process.
 *
 * <p>Registers Spine organization and the watched {@link #WATCHED_REPOS repositories} upon adding
 * the ChatBot to the space.
 */
final class SpineOrgInitProcess
        extends ProcessManager<OrganizationId, OrganizationInit, OrganizationInit.Builder>
        implements Logging {

    private static final String SPINE_ORG = "SpineEventEngine";

    private static final ImmutableList<String> WATCHED_REPOS = ImmutableList.of(
            "base", "time", "core-java", "web", "gcloud-java", "bootstrap", "money", "jdbc-storage"
    );

    /** The initialization process ID. **/
    static final OrganizationId SPINE_ORGANIZATION = organizationIdOf(SPINE_ORG);

    @LazyInit
    private @MonotonicNonNull TravisClient travisClient;

    /**
     * Registers {@link #SPINE_ORGANIZATION Spine} organization and watched resources that are
     * currently available in the Travis CI.
     *
     * <p>If a particular repository is not available in Travis, it is then skipped
     * and not registered.
     */
    @Command
    Iterable<CommandMessage> on(@External SpaceRegistered e) {
        if (state().isInitialized()) {
            return ImmutableSet.of();
        }
        var spaceId = e.getId();
        var commands = ImmutableSet.<CommandMessage>builder();
        commands.add(registerOrgCommand(SPINE_ORGANIZATION, spaceId.getValue()));
        travisClient.execute(ReposQuery.forOwner(SPINE_ORG))
                    .getRepositoriesList()
                    .stream()
                    .filter(repository -> WATCHED_REPOS.contains(repository.getName()))
                    .map(repository -> registerRepoCommand(repository, SPINE_ORGANIZATION))
                    .forEach(commands::add);
        builder().setGoogleChatSpace(spaceId.getValue())
                 .setInitialized(true);
        return commands.build();
    }

    private static RegisterRepository registerRepoCommand(Repository repo, OrganizationId orgId) {
        var slug = repo.getSlug();
        return RegisterRepository
                .newBuilder()
                .setOrganization(orgId)
                .setGithubUrl(githubRepoUrlFor(slug))
                .setId(repositoryIdOf(slug))
                .setName(repo.getName())
                .setTravisCiUrl(travisRepoUrlFor(slug))
                .vBuild();
    }

    private RegisterOrganization registerOrgCommand(OrganizationId spineOrgId, String spaceName) {
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

    void setTravisClient(TravisClient travisClient) {
        this.travisClient = travisClient;
    }
}
