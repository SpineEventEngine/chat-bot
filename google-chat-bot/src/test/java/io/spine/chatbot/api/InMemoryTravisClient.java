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

package io.spine.chatbot.api;

import io.spine.chatbot.travis.BuildsResponse;
import io.spine.chatbot.travis.RepositoriesResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An in-memory test-only implementation of the Travis CI API client.
 */
public final class InMemoryTravisClient implements TravisClient {

    private final Map<String, BuildsResponse> buildsResponses = new ConcurrentHashMap<>();
    private final Map<String, RepositoriesResponse> repositoriesResponses = new ConcurrentHashMap<>();

    @Override
    public BuildsResponse queryBuildsFor(String repoSlug) {
        checkNotNull(repoSlug);
        var result = buildsResponses.get(repoSlug);
        checkNotNull(result, "Builds response is not configured for the repository " + repoSlug);
        return result;
    }

    @Override
    public RepositoriesResponse queryRepositoriesFor(String owner) {
        checkNotNull(owner);
        var result = repositoriesResponses.get(owner);
        checkNotNull(result, "Repositories response is not configured for the owner " + owner);
        return result;
    }

    /** Sets up a mocked {@code builds} response for a specified {@code repoSlug}. **/
    public void setBuildsFor(String repoSlug, BuildsResponse builds) {
        checkNotNull(repoSlug);
        checkNotNull(builds);
        buildsResponses.put(repoSlug, builds);
    }

    /** Sets up a mocked {@code repositories} response for a specified {@code owner}. **/
    public void setRepositoriesFor(String owner, RepositoriesResponse repositories) {
        checkNotNull(owner);
        checkNotNull(repositories);
        repositoriesResponses.put(owner, repositories);
    }

    /** Resets state of the configured responses. **/
    public void reset() {
        buildsResponses.clear();
        repositoriesResponses.clear();
    }
}
