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

package io.spine.chatbot;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.spine.chatbot.client.Client;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.organization.Organization;
import io.spine.chatbot.github.repository.build.command.CheckRepositoryBuild;
import io.spine.logging.Logging;

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
        try (var client = Client.newInstance()) {
            var organizations = client.listOrganizations();
            for (var org : organizations) {
                var repos = client.listOrgRepos(org.getId());
                repos.forEach(repo -> checkBuildStatus(client, repo, org));
            }
            return "success";
        }
    }

    private void checkBuildStatus(Client client, RepositoryId repo, Organization org) {
        _debug().log("Sending `%s` command for the repository `%s`.",
                     CheckRepositoryBuild.class.getSimpleName(), repo.getValue());
        var checkRepositoryBuild = checkRepoBuildCommand(repo, org);
        client.post(checkRepositoryBuild);
    }

    private static CheckRepositoryBuild checkRepoBuildCommand(RepositoryId repo, Organization org) {
        return CheckRepositoryBuild
                .newBuilder()
                .setRepository(repo)
                .setOrganization(org.getId())
                .setSpace(org.space())
                .vBuild();
    }
}
