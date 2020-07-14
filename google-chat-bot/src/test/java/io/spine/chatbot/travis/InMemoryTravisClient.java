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

package io.spine.chatbot.travis;

import io.spine.chatbot.FailFastAwareClient;
import io.spine.chatbot.github.Slug;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.protobuf.Messages.defaultInstance;

/**
 * An in-memory test-only implementation of the Travis CI API client.
 */
public final class InMemoryTravisClient extends FailFastAwareClient implements TravisClient {

    private final Map<Query<?>, TravisResponse> responses = new ConcurrentHashMap<>();

    private InMemoryTravisClient(boolean failFast) {
        super(failFast);
    }

    /**
     * Creates a {@link #FailFastAwareClient#failFast failFast} in-memory Travis CI client.
     */
    public static InMemoryTravisClient strictClient() {
        return new InMemoryTravisClient(true);
    }

    /**
     * Creates a lenient in-memory Travis CI client.
     */
    public static InMemoryTravisClient lenientClient() {
        return new InMemoryTravisClient(false);
    }

    @Override
    public <T extends TravisResponse> T execute(Query<T> query) {
        checkNotNull(query);
        var stubbedValue = responses.get(query);
        var responseType = query.responseType();
        var result = failOrDefault(stubbedValue, query, defaultInstance(responseType));
        return responseType.cast(result);
    }

    /**
     * Sets up a stub {@code branchBuild} response for a specified {@code repository}.
     */
    public void setBuildsFor(Slug repository, RepoBranchBuildResponse branchBuild) {
        checkNotNull(repository);
        checkNotNull(branchBuild);
        responses.put(BuildsQuery.forRepo(repository), branchBuild);
    }

    /**
     * Sets up a stub {@code repositories} response for a specified {@code owner}.
     */
    public void setRepositoriesFor(Slug owner, RepositoriesResponse repositories) {
        checkNotNull(owner);
        checkNotNull(repositories);
        responses.put(ReposQuery.forOwner(owner), repositories);
    }

    /**
     * Resets state of the configured responses.
     */
    public void reset() {
        responses.clear();
    }
}
