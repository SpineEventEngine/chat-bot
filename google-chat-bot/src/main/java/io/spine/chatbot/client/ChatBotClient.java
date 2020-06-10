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

package io.spine.chatbot.client;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.organization.Organization;
import io.spine.chatbot.github.organization.OrganizationRepositories;
import io.spine.chatbot.github.organization.command.RegisterOrganization;
import io.spine.chatbot.github.organization.event.OrganizationRegistered;
import io.spine.chatbot.github.repository.command.RegisterRepository;
import io.spine.chatbot.github.repository.event.RepositoryRegistered;
import io.spine.client.Client;
import io.spine.client.ClientRequest;
import io.spine.client.Subscription;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.chatbot.server.github.Identifiers.newOrganizationId;
import static io.spine.chatbot.server.github.Identifiers.newRepositoryId;
import static io.spine.net.Urls.urlOfSpec;

public final class ChatBotClient {

    private final Client client;

    private ChatBotClient(Client client) {
        this.client = client;
    }

    /** Creates a new in-process client configured for the specified server. **/
    public static ChatBotClient inProcessClient(String serverName) {
        Client client = Client
                .inProcess(serverName)
                .build();
        return new ChatBotClient(client);
    }

    /** Returns Spine client quest request. **/
    public ClientRequest asGuest() {
        return client.asGuest();
    }

    /** Cancels the passed subscription. **/
    @CanIgnoreReturnValue
    public boolean cancelSubscription(Subscription subscription) {
        return client.subscriptions()
                     .cancel(subscription);
    }

    /**
     * Returns IDs for all registered listRepositories.
     */
    public ImmutableList<RepositoryId> listRepositories() {
        var orgIds = client.asGuest()
                           .select(Organization.class)
                           .run()
                           .stream()
                           .map(Organization::getId)
                           .collect(toImmutableList());
        var orgRepos = client.asGuest()
                             .select(OrganizationRepositories.class)
                             .byId(orgIds)
                             .run();
        var result = orgRepos.stream()
                             .map(OrganizationRepositories::getRepositoriesList)
                             .flatMap(Collection::stream)
                             .collect(toImmutableList());
        return result;
    }

    /**
     * Performs initialization of the entities that are monitored by the ChatBot
     * by default.
     */
    public void initializeDefaults() {
        var spineOrgId = newOrganizationId("SpineEventEngine");
        var registerSpineOrg = RegisterOrganization
                .newBuilder()
                .setName("Spine Event Engine")
                .setWebsiteUrl(urlOfSpec("https://spine.io/"))
                .setTravisCiUrl(urlOfSpec("https://travis-ci.com/github/SpineEventEngine"))
                .setGithubUrl(urlOfSpec("https://github.com/SpineEventEngine"))
                .setId(spineOrgId)
                .setGoogleChatSpace("spaces/AAAAnLxnh_o")
                .vBuild();
        postSyncCommand(registerSpineOrg, OrganizationRegistered.class);
        var registerSpineBase = RegisterRepository
                .newBuilder()
                .setOrganization(spineOrgId)
                .setGithubUrl(urlOfSpec("https://github.com/SpineEventEngine/base"))
                .setId(newRepositoryId("SpineEventEngine/base"))
                .setName("Spine Event Engine Base")
                .setTravisCiUrl(urlOfSpec("https://travis-ci.com/github/SpineEventEngine/base"))
                .vBuild();
        postSyncCommand(registerSpineBase, RepositoryRegistered.class);
    }

    private <E extends EventMessage> void
    postSyncCommand(CommandMessage command, Class<E> expectedOutcome) {
        postSyncCommand(command, expectedOutcome, 1);
    }

    private <E extends EventMessage> void
    postSyncCommand(CommandMessage command, Class<E> expectedOutcome, int expectedEvents) {
        var latch = new CountDownLatch(expectedEvents);
        var subscriptions = client
                .asGuest()
                .command(command)
                .observe(expectedOutcome, event -> latch.countDown())
                .post();
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Processing of command failed. Command: " + command, e);
        }
        subscriptions.forEach(this::cancelSubscription);
    }
}
