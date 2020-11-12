/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.concurrent.LazyInit;
import io.spine.base.CommandMessage;
import io.spine.chatbot.github.OrganizationId;
import io.spine.chatbot.github.organization.OrgHeader;
import io.spine.chatbot.github.organization.command.RegisterOrganization;
import io.spine.chatbot.github.organization.init.OrganizationInit;
import io.spine.chatbot.github.repository.RepoHeader;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.event.SpaceRegistered;
import io.spine.chatbot.travis.ReposQuery;
import io.spine.chatbot.travis.Repository;
import io.spine.chatbot.travis.TravisClient;
import io.spine.core.External;
import io.spine.logging.Logging;
import io.spine.net.Urls;
import io.spine.server.command.Command;
import io.spine.server.procman.ProcessManager;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static io.spine.chatbot.github.GitHubIdentifiers.organization;
import static io.spine.chatbot.github.GitHubIdentifiers.repository;
import static io.spine.chatbot.github.Slugs.newSlug;
import static io.spine.chatbot.github.Slugs.orgSlug;
import static io.spine.chatbot.net.MoreUrls.githubUrlFor;
import static io.spine.chatbot.net.MoreUrls.travisUrlFor;

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

    /**
     * The initialization process ID.
     */
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
        _info().log("Starting Spine organization initialization process in the space `%s`.", space);
        var commands = ImmutableSet.<CommandMessage>builder();
        commands.add(registerOrgCommand(ORGANIZATION, space));
        client.execute(ReposQuery.forOwner(orgSlug(ORGANIZATION)))
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
        var slug = newSlug(repo.getSlug());
        _info().log("Registering `%s` repository.", slug.getValue());
        var header = RepoHeader
                .newBuilder()
                .setOrganization(org)
                .setGithubProfile(githubUrlFor(slug))
                .setName(repo.getName())
                .setTravisProfile(travisUrlFor(slug))
                .vBuild();
        return RegisterRepository
                .newBuilder()
                .setId(repository(slug.getValue()))
                .setHeader(header)
                .vBuild();
    }

    private RegisterOrganization registerOrgCommand(OrganizationId spineOrg, SpaceId space) {
        var slug = orgSlug(spineOrg);
        _info().log("Registering `%s` organization.", spineOrg.getValue());
        var header = OrgHeader
                .newBuilder()
                .setName("Spine Event Engine")
                .setWebsite(Urls.create("https://spine.io/"))
                .setTravisProfile(travisUrlFor(slug))
                .setGithubProfile(githubUrlFor(slug))
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
