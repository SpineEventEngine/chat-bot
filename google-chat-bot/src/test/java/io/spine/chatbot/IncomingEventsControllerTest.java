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

package io.spine.chatbot;

import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PubsubPushNotification;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import io.spine.chatbot.api.travis.InMemoryTravisClient;
import io.spine.chatbot.server.github.GitHubContext;
import io.spine.chatbot.server.google.chat.GoogleChatContext;
import io.spine.json.Json;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.micronaut.http.HttpRequest.POST;
import static io.spine.chatbot.Application.startSpineServer;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("IncomingEventsController should")
@MicronautTest
final class IncomingEventsControllerTest {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @BeforeAll
    static void setupServer() {
        var chatContext = GoogleChatContext
                .newBuilder()
                .build();
        var gitHubContext = GitHubContext
                .newBuilder()
                .setTravis(InMemoryTravisClient.lenientClient())
                .build();
        startSpineServer(gitHubContext, chatContext);
    }

    @Test
    @DisplayName("receive and decode a pubsub message with Google Chat event")
    void receiveAndDecode() {
        var pubsubMessage = PubsubMessage
                .newBuilder()
                .setMessageId("129y418y4houfhiuehwr")
                .setData(base64Encode(CHAT_MESSAGE_EVENT))
                .build();
        var pushNotification = PubsubPushNotification
                .newBuilder()
                .setMessage(pubsubMessage)
                .setSubscription("projects/test-project/subscriptions/test-subscription")
                .vBuild();
        var request = POST("/chat/incoming/event", Json.toJson(pushNotification))
                .contentType(MediaType.APPLICATION_JSON);
        String actual = client.toBlocking()
                              .retrieve(request);
        assertEquals("OK", actual);
    }

    private static ByteString base64Encode(String value) {
        var encoder = Base64.getEncoder();
        return ByteString.copyFrom(encoder.encode(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static final String CHAT_MESSAGE_EVENT = "" +
            "{\n" +
            "  \"type\":\"MESSAGE\",\n" +
            "  \"eventTime\":\"2017-03-02T19:02:59.910959Z\",\n" +
            "  \"space\":{\n" +
            "    \"name\":\"spaces/AAAAAAAAAAA\",\n" +
            "    \"displayName\":\"Chuck Norris Discussion Room\",\n" +
            "    \"type\":\"ROOM\"\n" +
            "  },\n" +
            "  \"message\":{\n" +
            "    \"name\":\"spaces/AAAAAAAAAAA/messages/CCCCCCCCCCC\",\n" +
            "    \"sender\":{\n" +
            "      \"name\":\"users/12345678901234567890\",\n" +
            "      \"displayName\":\"Chuck Norris\",\n" +
            "      \"avatarUrl\":\"https://lh3.googleusercontent.com/.../photo.jpg\",\n" +
            "      \"email\":\"chuck@example.com\"\n" +
            "    },\n" +
            "    \"createTime\":\"2017-03-02T19:02:59.910959Z\",\n" +
            "    \"text\":\"@TestBot Violence is my last option.\",\n" +
            "    \"argumentText\":\" Violence is my last option.\",\n" +
            "    \"thread\":{\n" +
            "      \"name\":\"spaces/AAAAAAAAAAA/threads/BBBBBBBBBBB\"\n" +
            "    },\n" +
            "    \"annotations\":[\n" +
            "      {\n" +
            "        \"length\":8,\n" +
            "        \"startIndex\":0,\n" +
            "        \"userMention\":{\n" +
            "          \"type\":\"MENTION\",\n" +
            "          \"user\":{\n" +
            "            \"avatarUrl\":\"https://.../avatar.png\",\n" +
            "            \"displayName\":\"TestBot\",\n" +
            "            \"name\":\"users/1234567890987654321\",\n" +
            "            \"type\":\"BOT\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"type\":\"USER_MENTION\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"user\":{\n" +
            "    \"name\":\"users/12345678901234567890\",\n" +
            "    \"displayName\":\"Chuck Norris\",\n" +
            "    \"avatarUrl\":\"https://lh3.googleusercontent.com/.../photo.jpg\",\n" +
            "    \"email\":\"chuck@example.com\"\n" +
            "  }\n" +
            "}";
}
