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

import io.spine.chatbot.api.travis.Travis;
import io.spine.chatbot.api.travis.TravisClient;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides {@link BoundedContextBuilder} for the GitHub context.
 */
public final class GitHubContext {

    /**
     * The name of the GitHub Context.
     */
    static final String GIT_HUB_CONTEXT_NAME = "GitHub";

    private final BoundedContextBuilder contextBuilder;

    private GitHubContext(TravisClient travisClient) {
        this.contextBuilder = configureContextBuilder(travisClient);
    }

    /**
     * Returns the context builder associated with the GitHub context.
     */
    public BoundedContextBuilder contextBuilder() {
        return this.contextBuilder;
    }

    private static BoundedContextBuilder configureContextBuilder(TravisClient travisClient) {
        return BoundedContext
                .singleTenant(GIT_HUB_CONTEXT_NAME)
                .add(OrganizationAggregate.class)
                .add(RepositoryAggregate.class)
                .add(new OrgReposRepository())
                .add(new SpineOrgInitRepository(travisClient))
                .add(new RepoBuildRepository(travisClient));
    }

    /**
     * Creates a new builder of the GitHub context.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A Builder for configuring GitHub context.
     */
    public static final class Builder {

        private TravisClient travisClient;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        /**
         * Sets Travis CI client to be used within the context.
         */
        public Builder setTravis(TravisClient travisClient) {
            checkNotNull(travisClient);
            this.travisClient = travisClient;
            return this;
        }

        /**
         * Finishes configuration of the context and builds a new instance.
         *
         * <p>If the {@link #travisClient} was not explicitly configured, uses the
         * {@link Travis#newInstance() default} client.
         */
        public GitHubContext build() {
            if (travisClient == null) {
                travisClient = Travis.newInstance();
            }
            return new GitHubContext(travisClient);
        }
    }
}
