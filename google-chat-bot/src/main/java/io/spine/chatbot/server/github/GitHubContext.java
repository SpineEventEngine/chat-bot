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

import com.google.errorprone.annotations.concurrent.LazyInit;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.QueryService;
import io.spine.server.commandbus.CommandBus;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * Provides BoundedContestBuilder for the GitHub Context.
 */
public final class GitHubContext {

    @MonotonicNonNull
    @LazyInit
    private static BoundedContext context = null;

    @MonotonicNonNull
    @LazyInit
    private static QueryService queryService = null;

    /**
     * The name of the Context.
     */
    static final String NAME = "GitHub";

    /**
     * Prevents instantiation of this utility class.
     */
    private GitHubContext() {
    }

    public static CommandBus commandBus() {
        return context().commandBus();
    }

    public static synchronized QueryService queryService() {
        if (queryService == null) {
            queryService = QueryService
                    .newBuilder()
                    .add(context())
                    .build();
        }
        return queryService;
    }

    public static synchronized BoundedContext context() {
        if (context == null) {
            context = newBuilder().build();
        }
        return context;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void initialize() {
        queryService();
    }

    /**
     * Creates a new instance of the GitHub Context builder.
     */
    public static BoundedContextBuilder newBuilder() {
        return BoundedContext
                .singleTenant(NAME)
                .add(OrganizationAggregate.class)
                .add(RepositoryAggregate.class)
                .add(RepositoryBuildProcess.class)
                .add(new OrganizationRepositoriesRepository());
    }
}
