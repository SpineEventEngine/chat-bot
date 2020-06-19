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

import com.google.protobuf.Message;
import io.spine.chatbot.travis.BuildsResponse;
import io.spine.chatbot.travis.RepositoriesResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

import static io.spine.chatbot.api.JsonProtoBodyHandler.jsonBodyHandler;
import static java.lang.String.format;

/**
 * A Travis CI REST API client.
 *
 * @see <a href="https://developer.travis-ci.com/">Travis CI API</a>
 */
public final class Travis implements TravisClient {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://api.travis-ci.com";
    private static final String API_HEADER = "Travis-API-Version";
    private static final String API_VERSION = "3";
    private static final String AUTH_HEADER = "Authorization";

    private final String apiToken;

    /**
     * Creates a new Travis client with the specified API token.
     */
    private Travis(String token) {
        apiToken = token;
    }

    /**
     * Creates a new Travis client with the default secure Travis token.
     */
    public static TravisClient defaultTravisClient() {
        return new Travis(Secrets.travisToken());
    }

    @Override
    public BuildsResponse queryBuildsFor(String repoSlug) {
        var encodedSlug = URLEncoder.encode(repoSlug, StandardCharsets.UTF_8);
        var repositoryBuildsQuery = "/repo/"
                + encodedSlug
                + "/builds?limit=1&branch.name=master&include=build.commit";
        var result = queryForResponse(repositoryBuildsQuery, BuildsResponse.class);
        return result;
    }

    @Override
    public RepositoriesResponse queryRepositoriesFor(String owner) {
        var encodedOwner = URLEncoder.encode(owner, StandardCharsets.UTF_8);
        var ownerRepositoriesQuery = "/owner/" + encodedOwner + "/repos";
        var result = queryForResponse(ownerRepositoriesQuery, RepositoriesResponse.class);
        return result;
    }

    private <T extends Message> T queryForResponse(String query, Class<T> responseType) {
        var request = apiRequest(query, apiToken);
        try {
            var result = CLIENT.send(request, jsonBodyHandler(responseType));
            return result.body();
        } catch (IOException | InterruptedException e) {
            var message = format("Unable to query data for response of type '%s' using query '%s'.",
                                 responseType, query);
            throw new RuntimeException(message, e);
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