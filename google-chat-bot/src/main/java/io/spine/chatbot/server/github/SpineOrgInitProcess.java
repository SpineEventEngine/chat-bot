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
import io.spine.chatbot.github.organization.OrgHeader;
import io.spine.chatbot.github.organization.command.RegisterOrganization;
import io.spine.chatbot.github.organization.init.OrganizationInit;
import io.spine.chatbot.github.repository.RepoHeader;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.event.SpaceRegistered;
import io.spine.core.External;
import io.spine.logging.Logging;
import io.spine.net.Urls;
import io.spine.server.command.Command;
import io.spine.server.procman.ProcessManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static io.spine.chatbot.server.github.GitHubIdentifier.organization;
import static io.spine.chatbot.server.github.GitHubIdentifier.repository;
import static io.spine.chatbot.url.MoreUrls.githubUrlFor;
import static io.spine.chatbot.url.MoreUrls.travisUrlFor;

/**
 * Spine organization init process.
 *
 * <p>Registers Spine organization and the watched {@link #WATCHED_REPOS repositories} upon adding
 * the ChatBot to the space.
 */
final class SpineOrgInitProcess
        extends ProcessManager<OrganizationId, OrganizationInit, OrganizationInit.Builder>
        implements Logging {

    private static final ImmutableList<String> WATCHED_REPOS = ImmutableList.of(
            "base", "time", "core-java", "web", "gcloud-java", "bootstrap", "money", "jdbc-storage"
    );

    /** The initialization process ID. **/
    static final OrganizationId ORGANIZATION = organization("SpineEventEngine");

    @LazyInit
    private @MonotonicNonNull TravisClient client;

    /**
     * Registers {@link #ORGANIZATION Spine} organization and watched resources that are
     * currently available in the Travis CI.
     *
     * <p>If a particular repository is not available in Travis, it is then skipped
     * and not registered.
     */
    @Command
    Iterable<CommandMessage> on(@External SpaceRegistered e) {
        if (state().getInitialized()) {
            _info().log("Spine organization is already initialized. Skipping the process.");
            return ImmutableSet.of();
        }
        var space = e.getSpace();
        _info().log("Starting Spine organization initialization process in space `%s`.", space);
        var commands = ImmutableSet.<CommandMessage>builder();
        commands.add(registerOrgCommand(ORGANIZATION, space));
        client.execute(ReposQuery.forOwner(Slugs.forOrg(ORGANIZATION)))
              .getRepositoriesList()
              .stream()
              .filter(repository -> WATCHED_REPOS.contains(repository.getName()))
              .map(repository -> registerRepoCommand(repository, ORGANIZATION))
              .forEach(commands::add);
        builder().setSpace(space)
                 .setInitialized(true);
        return commands.build();
    }

    private RegisterRepository registerRepoCommand(Repository repo, OrganizationId org) {
        var slug = repo.getSlug();
        _info().log("Registering `%s` repository.", slug);
        var header = RepoHeader
                .newBuilder()
                .setOrganization(org)
                .setGithubProfile(githubUrlFor(slug))
                .setName(repo.getName())
                .setTravisProfile(travisUrlFor(slug))
                .vBuild();
        return RegisterRepository
                .newBuilder()
                .setId(repository(slug))
                .setHeader(header)
                .vBuild();
    }

    private RegisterOrganization registerOrgCommand(OrganizationId spineOrg, SpaceId space) {
        _info().log("Registering `%s` organization.", ORGANIZATION.getValue());
        var header = OrgHeader
                .newBuilder()
                .setName("Spine Event Engine")
                .setWebsite(Urls.create("https://spine.io/"))
                .setTravisProfile(travisUrlFor(ORGANIZATION.getValue()))
                .setGithubProfile(githubUrlFor(ORGANIZATION.getValue()))
                .setSpace(space)
                .vBuild();
        return RegisterOrganization
                .newBuilder()
                .setId(spineOrg)
                .setHeader(header)
                .vBuild();
    }

    /**
     * Sets {@link #client} to be used during handling of signals.
     *
     * @implNote the method is intended to be used as part of the entity configuration
     *         done through the repository
     */
    void setClient(TravisClient client) {
        this.client = client;
    }
}
