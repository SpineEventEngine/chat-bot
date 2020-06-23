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
import io.spine.chatbot.github.OrganizationId;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.organization.Organization;
import io.spine.chatbot.github.organization.OrganizationRepositories;
import io.spine.client.Client;
import io.spine.client.ClientRequest;
import io.spine.client.CommandRequest;
import io.spine.client.Subscription;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A ChatBot application's Spine client.
 *
 * <p>Abstracts working with Spine's {@link Client client}.
 */
public final class ChatBotServerClient {

    private final Client client;

    private ChatBotServerClient(Client client) {
        this.client = client;
    }

    /**
     * Creates a new in-process client configured for the specified server.
     */
    public static ChatBotServerClient inProcessClient(String serverName) {
        Client client = Client
                .inProcess(serverName)
                .build();
        return new ChatBotServerClient(client);
    }

    /**
     * Returns Spine client guest request.
     */
    public ClientRequest asGuest() {
        return client.asGuest();
    }

    /**
     * Cancels the passed subscription.
     *
     * @see io.spine.client.Subscriptions#cancel(Subscription)
     * @see CommandRequest#post()
     */
    @CanIgnoreReturnValue
    public boolean cancelSubscription(Subscription subscription) {
        return client.subscriptions()
                     .cancel(subscription);
    }

    /**
     * Retrieves all registered organizations.
     */
    public ImmutableList<Organization> listOrganizations() {
        return client.asGuest()
                     .select(Organization.class)
                     .run();
    }

    /**
     * Returns list of all registered repositories for the {@code organization}.
     */
    public ImmutableList<RepositoryId> listOrgRepos(OrganizationId organization) {
        var orgRepos = client.asGuest()
                             .select(OrganizationRepositories.class)
                             .byId(organization)
                             .run();
        checkState(orgRepos.size() == 1);
        return ImmutableList.copyOf(orgRepos.get(0)
                                            .getRepositoriesList());
    }

    /**
     * Returns IDs for all registered repositories.
     */
    public ImmutableList<RepositoryId> listRepositories() {
        var orgIds = client.asGuest()
                           .select(Organization.class)
                           .run()
                           .stream()
                           .map(Organization::getOrganization)
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
     * Posts a command and waits synchronously till the expected outcome event is published.
     */
    public <E extends EventMessage> void post(CommandMessage command, Class<E> expectedOutcome) {
        post(command, expectedOutcome, 1);
    }

    private <E extends EventMessage> void
    post(CommandMessage command, Class<E> expectedOutcome, int expectedEvents) {
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
