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

package io.spine.chatbot;

import com.google.common.collect.ImmutableList;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.spine.chatbot.client.ChatBotClient;
import io.spine.chatbot.github.OrganizationId;
import io.spine.chatbot.github.organization.command.RegisterOrganization;
import io.spine.chatbot.github.organization.event.OrganizationRegistered;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.chatbot.github.repository.event.RepositoryRegistered;
import io.spine.chatbot.travis.Repository;
import io.spine.logging.Logging;

import static io.spine.chatbot.api.TravisClient.defaultTravisClient;
import static io.spine.chatbot.server.github.Identifiers.organizationIdOf;
import static io.spine.chatbot.server.github.Identifiers.repositoryIdOf;
import static io.spine.net.Urls.githubRepoUrlFor;
import static io.spine.net.Urls.travisRepoUrlFor;
import static io.spine.net.Urls.urlOfSpec;

/**
 * A REST controller for handling initialization of the application state.
 */
@Controller("/init")
class InitController implements Logging {

    private static final ImmutableList<String> WATCHED_REPOS = ImmutableList.of(
            "base", "time", "core-java", "web", "gcloud-java", "bootstrap", "money", "jdbc-storage"
    );

    /**
     * Performs the initial registration of the default Spine resources the bot is going
     * to watch for for a particular Google Chat space.
     */
    @Get
    public String initWatchedResources(@QueryValue String spaceName) {
        _info().log(
                "Performing initial application state initialization in the Google Chat space `%s`",
                spaceName);
        var client = ChatBotClient.inProcessClient(Application.SERVER_NAME);
        var spineOrgId = organizationIdOf("SpineEventEngine");
        registerOrganization(client, spineOrgId, spaceName);
        registerWatchedRepos(client, spineOrgId);
        return "Successfully initialized";
    }

    private void registerWatchedRepos(ChatBotClient client, OrganizationId spineOrgId) {
        var watchedRepositories = defaultTravisClient()
                .queryRepositoriesFor(spineOrgId.getValue())
                .getRepositoriesList()
                .stream()
                .filter(repository -> WATCHED_REPOS.contains(repository.getName()));
        watchedRepositories.forEach(repository -> {
            _info().log("Sending `RegisterRepository` command for repository `%s`.",
                        repository.getName());
            var registerRepository = registerRepoCommand(repository, spineOrgId);
            client.postSyncCommand(registerRepository, RepositoryRegistered.class);
        });
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

    private void registerOrganization(ChatBotClient client,
                                      OrganizationId spineOrgId,
                                      String spaceName) {
        _info().log("Registering `Spine Event Engine` organization.");
        var registerSpineOrg = RegisterOrganization
                .newBuilder()
                .setName("Spine Event Engine")
                .setWebsiteUrl(urlOfSpec("https://spine.io/"))
                .setTravisCiUrl(urlOfSpec("https://travis-ci.com/github/SpineEventEngine"))
                .setGithubUrl(urlOfSpec("https://github.com/SpineEventEngine"))
                .setId(spineOrgId)
                .setGoogleChatSpace(spaceName)
                .vBuild();
        client.postSyncCommand(registerSpineOrg, OrganizationRegistered.class);
    }
}
