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
import io.spine.chatbot.github.organization.Organization;
import io.spine.chatbot.github.organization.OrganizationRepositories;
import io.spine.client.Client;

import java.util.Collection;
import java.util.stream.Collectors;

import static io.spine.chatbot.Application.SERVER_NAME;

@Controller("/cron")
public class CronController {

    @Post("/repositories/check")
    public String checkRepositoryStatuses() {
        Client client = Client
                .inProcess(SERVER_NAME)
                .build();
        var orgIds = client.asGuest()
                           .select(Organization.class)
                           .run()
                           .stream()
                           .map(Organization::getId)
                           .collect(Collectors.toList());
        var orgRepos = client.asGuest()
                             .select(OrganizationRepositories.class)
                             .byId(orgIds)
                             .run();
        var repositoryIds = orgRepos.stream()
                                    .map(OrganizationRepositories::getRepositoriesList)
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList());

        return "success";
    }
}