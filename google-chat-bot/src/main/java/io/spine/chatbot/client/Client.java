/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.chatbot.client;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.base.CommandMessage;
import io.spine.base.EventMessage;
import io.spine.chatbot.github.OrganizationId;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.organization.Organization;
import io.spine.chatbot.github.organization.OrganizationRepositories;
import io.spine.chatbot.server.Server;
import io.spine.client.CommandRequest;
import io.spine.client.Subscription;

import java.util.concurrent.CountDownLatch;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A ChatBot application's Spine client.
 *
 * <p>Abstracts working with Spine's {@link io.spine.client.Client client}.
 */
public final class Client implements AutoCloseable {

    private final io.spine.client.Client client;

    private Client(io.spine.client.Client client) {
        this.client = client;
    }

    /**
     * Creates a new in-process client linked to the {@link Server}.
     */
    public static Client newInstance() {
        var client = io.spine.client.Client
                .inProcess(Server.name())
                .build();
        return new Client(client);
    }

    /**
     * Retrieves all registered organizations.
     */
    public ImmutableList<Organization> listOrganizations() {
        var query = Organization.query()
                                .build();
        return client.asGuest()
                     .run(query);
    }

    /**
     * Returns list of all registered repositories for the {@code organization}.
     */
    public ImmutableList<RepositoryId> listOrgRepos(OrganizationId org) {
        checkNotNull(org);
        var query =
                OrganizationRepositories.query()
                                        .organization()
                                        .is(org)
                                        .build();
        var orgRepos = client.asGuest()
                             .run(query);
        checkState(orgRepos.size() == 1);
        var repos = orgRepos.get(0);
        return ImmutableList.copyOf(repos.getRepositoryList());
    }

    @Override
    public void close() {
        this.client.close();
    }

    /**
     * Posts a command and waits synchronously till the expected outcome event is published.
     */
    public <E extends EventMessage> void post(CommandMessage command, Class<E> expectedOutcome) {
        checkNotNull(command);
        checkNotNull(expectedOutcome);
        post(command, expectedOutcome, 1);
    }

    /**
     * Posts a command asynchronously.
     *
     * @see #post(CommandMessage, Class)
     */
    public void post(CommandMessage command) {
        checkNotNull(command);
        client.asGuest()
              .command(command)
              .onStreamingError(Client::throwProcessingError)
              .postAndForget();
    }

    private <E extends EventMessage> void
    post(CommandMessage command, Class<E> expectedOutcome, int expectedEvents) {
        var latch = new CountDownLatch(expectedEvents);
        var subscriptions = client.asGuest()
                                  .command(command)
                                  .onStreamingError(Client::throwProcessingError)
                                  .observe(expectedOutcome, event -> latch.countDown())
                                  .post();
        try {
            latch.await();
        } catch (InterruptedException e) {
            newIllegalStateException(e, "Processing of command interrupted:%n%s.", command);
        }
        subscriptions.forEach(this::cancelSubscription);
    }

    /**
     * Cancels the passed subscription.
     *
     * @see io.spine.client.Subscriptions#cancel(Subscription)
     * @see CommandRequest#post()
     */
    @CanIgnoreReturnValue
    private boolean cancelSubscription(Subscription subscription) {
        checkNotNull(subscription);
        return client.subscriptions()
                     .cancel(subscription);
    }

    private static void throwProcessingError(Throwable throwable) {
        throw newIllegalStateException(
                throwable, "An error while processing the command."
        );
    }
}
