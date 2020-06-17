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
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.spine.chatbot.client.ChatBotClient;
import io.spine.chatbot.github.OrganizationId;
import io.spine.chatbot.github.organization.command.RegisterOrganization;
import io.spine.chatbot.github.organization.event.OrganizationRegistered;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.chatbot.github.repository.event.RepositoryRegistered;
import io.spine.logging.Logging;

import static io.spine.chatbot.server.github.Identifiers.newOrganizationId;
import static io.spine.chatbot.server.github.Identifiers.newRepositoryId;
import static io.spine.net.Urls.urlOfSpec;

/**
 * A REST controller for handling initialization of the application state.
 */
@Controller("/init")
public class InitController implements Logging {

    /**
     * Performs the initial registration of the default Spine resources the bot is going
     * to watch for for a particular Google Chat space.
     */
    @Get
    public String initWatchedResources(@QueryValue String spaceName) {
        _info().log("Performing initial application state initialization.");
        var client = ChatBotClient.inProcessClient(Application.SERVER_NAME);
        var spineOrgId = newOrganizationId("SpineEventEngine");
        registerOrganization(client, spineOrgId, spaceName);
        registerBase(client, spineOrgId);
        return "Successfully initialized";
    }

    private static void registerBase(ChatBotClient client, OrganizationId spineOrgId) {
        var registerSpineBase = RegisterRepository
                .newBuilder()
                .setOrganization(spineOrgId)
                .setGithubUrl(urlOfSpec("https://github.com/SpineEventEngine/base"))
                .setId(newRepositoryId("SpineEventEngine/base"))
                .setName("Spine Event Engine Base")
                .setTravisCiUrl(urlOfSpec("https://travis-ci.com/github/SpineEventEngine/base"))
                .vBuild();
        client.postSyncCommand(registerSpineBase, RepositoryRegistered.class);
    }

    private static void registerOrganization(ChatBotClient client,
                                             OrganizationId spineOrgId,
                                             String spaceName) {
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
