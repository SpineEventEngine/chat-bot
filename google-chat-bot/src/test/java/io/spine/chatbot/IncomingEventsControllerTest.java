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

package io.spine.chatbot;

import com.google.common.io.Resources;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.spine.chatbot.google.chat.InMemoryGoogleChatClient;
import io.spine.chatbot.server.Server;
import io.spine.chatbot.server.github.GitHubContext;
import io.spine.chatbot.server.google.chat.GoogleChatContext;
import io.spine.chatbot.travis.InMemoryTravisClient;
import io.spine.json.Json;
import io.spine.pubsub.PubsubPushRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.micronaut.http.HttpRequest.POST;
import static io.spine.util.Exceptions.newIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
@DisplayName("`IncomingEventsController` should")
final class IncomingEventsControllerTest {

    @Inject
    @Client("/")
    private HttpClient client;

    @BeforeAll
    static void setupServer() {
        var chatContext = GoogleChatContext
                .newBuilder()
                .setClient(InMemoryGoogleChatClient.lenientClient())
                .build();
        var gitHubContext = GitHubContext
                .newBuilder()
                .setTravis(InMemoryTravisClient.lenientClient())
                .build();
        Server.withContexts(chatContext, gitHubContext)
              .start();
    }

    @Test
    @DisplayName("receive and decode a Pub/Sub message with Google Chat event")
    void receiveAndDecode() {
        var pubsubMessage = PubsubMessage
                .newBuilder()
                .setMessageId("129y418y4houfhiuehwr")
                .setData(ByteString.copyFromUtf8(chatEventJson()))
                .build();
        var pushRequest = PubsubPushRequest
                .newBuilder()
                .setMessage(pubsubMessage)
                .setSubscription("projects/test-project/subscriptions/test-subscription")
                .vBuild();
        var request = POST("/chat/incoming/event", Json.toJson(pushRequest))
                .contentType(MediaType.APPLICATION_JSON);
        String actual = client.toBlocking()
                              .retrieve(request);
        assertEquals("OK", actual);
    }

    private static String chatEventJson() {
        try {
            var resource = Resources.getResource("chat_event.json");
            return Resources.toString(resource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to load ChatEvent message JSON definition.");
        }
    }
}
