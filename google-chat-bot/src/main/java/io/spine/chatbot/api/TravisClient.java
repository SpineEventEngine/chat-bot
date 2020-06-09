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

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

import static io.spine.chatbot.api.JsonProtoBodyHandler.jsonBodyHandler;

/**
 * A Travis CI API client.
 */
public final class TravisClient {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://api.travis-ci.com";
    private static final String API_HEADER = "Travis-API-Version";
    private static final String API_VERSION = "3";
    private static final String AUTH_HEADER = "Authorization";

    private final String apiToken;

    public TravisClient(String token) {
        apiToken = token;
    }

    /**
     * Queries Travis CI build statuses for a repository determined by the supplied repository slug.
     */
    public BuildsResponse queryBuildsFor(String repoSlug) {
        var encodedSlug = URLEncoder.encode(repoSlug, StandardCharsets.UTF_8);
        var repoBuilds = "/repo/"
                + encodedSlug
                + "/builds?limit=1&branch.name=master?include=build.commit";
        var request = apiRequest(repoBuilds, apiToken);
        try {
            var result = CLIENT.send(request, jsonBodyHandler(BuildsResponse.class));
            return result.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to retrieve repository " + repoSlug + " builds.", e);
        }
    }

    private static HttpRequest apiRequest(String apiPath, String token) {
        return authorizedApiRequest(token)
                .uri(URI.create(BASE_URL + apiPath))
                .build();
    }

    private static HttpRequest.Builder authorizedApiRequest(String token) {
        return HttpRequest
                .newBuilder()
                .GET()
                .header(API_HEADER, API_VERSION)
                .header(AUTH_HEADER, "token " + token);
    }
}
