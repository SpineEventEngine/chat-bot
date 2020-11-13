/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.chatbot.travis;

import io.spine.logging.Logging;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static com.google.api.client.util.Preconditions.checkNotNull;
import static io.spine.chatbot.travis.JsonProtoBodyHandler.jsonBodyHandler;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A client to the Travis CI REST API.
 *
 * @see <a href="https://developer.travis-ci.com/">Travis CI API</a>
 */
final class Travis implements TravisClient, Logging {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://api.travis-ci.com";
    private static final String API_HEADER = "Travis-API-Version";
    private static final String API_VERSION = "3";
    private static final String AUTH_HEADER = "Authorization";

    private final Token apiToken;

    /**
     * Creates a new Travis client with the specified API token.
     */
    Travis(Token apiToken) {
        this.apiToken = checkNotNull(apiToken);
    }

    @Override
    public <T extends TravisResponse> T execute(Query<T> query) {
        var result = execute(query.request(), query.responseType());
        return result;
    }

    private <T extends TravisResponse> T execute(String request, Class<T> responseType) {
        var apiRequest = apiRequest(request, apiToken);
        try {
            _trace().log("Executing Travis API request `%s` for response `%s`.",
                         request, responseType.getSimpleName());
            var result = CLIENT.send(apiRequest, jsonBodyHandler(responseType));
            return result.body();
        } catch (IOException | InterruptedException e) {
            throw newIllegalStateException(
                    e, "Unable to query data for response of type '%s' using request '%s'.",
                    responseType, request
            );
        }
    }

    private static HttpRequest apiRequest(String request, Token token) {
        return authorizedApiRequest(token)
                .uri(URI.create(BASE_URL + request))
                .build();
    }

    private static HttpRequest.Builder authorizedApiRequest(Token token) {
        return HttpRequest
                .newBuilder()
                .GET()
                .header(API_HEADER, API_VERSION)
                .header(AUTH_HEADER, "token " + token.value());
    }
}
