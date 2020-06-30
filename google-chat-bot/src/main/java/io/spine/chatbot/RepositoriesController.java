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

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.spine.chatbot.client.Client;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.organization.Organization;
import io.spine.chatbot.github.repository.build.command.CheckRepositoryBuild;
import io.spine.logging.Logging;

import static io.spine.chatbot.Application.SERVER_NAME;

/**
 * A REST controller handling Repository commands.
 */
@Controller("/repositories")
final class RepositoriesController implements Logging {

    /**
     * Sends {@link CheckRepositoryBuild} commands to all repositories registered in the system.
     */
    @Post("/builds/check")
    String checkBuildStatuses() {
        _debug().log("Checking repositories build statuses.");
        try (var client = Client.inProcessClient(SERVER_NAME)) {
            var organizations = client.listOrganizations();
            for (var organization : organizations) {
                var repos = client.listOrgRepos(organization.getId());
                repos.forEach(repo -> checkBuildStatus(client, repo, organization));
            }
            return "success";
        }
    }

    private void checkBuildStatus(Client client,
                                  RepositoryId repository,
                                  Organization organization) {
        _info().log("Sending `CheckRepositoryBuild` command for the repository `%s`.",
                    repository.getValue());
        var checkRepositoryBuild = checkRepoBuildCommand(repository, organization);
        client.post(checkRepositoryBuild);
    }

    private static CheckRepositoryBuild
    checkRepoBuildCommand(RepositoryId repository, Organization organization) {
        return CheckRepositoryBuild
                .newBuilder()
                .setRepository(repository)
                .setOrganization(organization.getId())
                .setGoogleChatSpace(organization.getGoogleChatSpace())
                .vBuild();
    }
}
