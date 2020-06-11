package io.spine.chatbot;

import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PubsubPushNotification;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import io.spine.json.Json;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.micronaut.http.HttpRequest.POST;
import static io.spine.chatbot.Application.initializeSpine;
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
        initializeSpine();
    }

    @Test
    @DisplayName("receive and decode a pubsub message with Google Chat event")
    void receiveAndDecode() {
        //TODO:2020-06-11:ysergiichuk: make sure we handle serialization/deserialization of the
        // chat events properly. Looks like the JSON below could not be deserialized due to internal
        // proto issue
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
        assertEquals("success", actual);
    }

    private static ByteString base64Encode(String value) {
        var encoder = Base64.getEncoder();
        return ByteString.copyFrom(encoder.encode(value.getBytes(StandardCharsets.UTF_8)));
    }

    private static final String CHAT_MESSAGE_EVENT = "" +
            "{\n" +
            "  \"type\": \"MESSAGE\",\n" +
            "  \"eventTime\": \"2017-03-02T19:02:59.910959Z\",\n" +
            "  \"space\": {\n" +
            "    \"name\": \"spaces/AAAAAAAAAAA\",\n" +
            "    \"displayName\": \"Chuck Norris Discussion Room\",\n" +
            "    \"type\": \"ROOM\"\n" +
            "  },\n" +
            "  \"message\": {\n" +
            "    \"name\": \"spaces/AAAAAAAAAAA/messages/CCCCCCCCCCC\",\n" +
            "    \"sender\": {\n" +
            "      \"name\": \"users/12345678901234567890\",\n" +
            "      \"displayName\": \"Chuck Norris\",\n" +
            "      \"avatarUrl\": \"https://lh3.googleusercontent.com/.../photo.jpg\",\n" +
            "      \"email\": \"chuck@example.com\"\n" +
            "    },\n" +
            "    \"createTime\": \"2017-03-02T19:02:59.910959Z\",\n" +
            "    \"text\": \"@TestBot Violence is my last option.\",\n" +
            "    \"argumentText\": \" Violence is my last option.\",\n" +
            "    \"thread\": {\n" +
            "      \"name\": \"spaces/AAAAAAAAAAA/threads/BBBBBBBBBBB\"\n" +
            "    },\n" +
            "    \"annotations\": [\n" +
            "      {\n" +
            "        \"length\": 8,\n" +
            "        \"startIndex\": 0,\n" +
            "        \"userMention\": {\n" +
            "          \"type\": \"MENTION\",\n" +
            "          \"user\": {\n" +
            "            \"avatarUrl\": \"https://.../avatar.png\",\n" +
            "            \"displayName\": \"TestBot\",\n" +
            "            \"name\": \"users/1234567890987654321\",\n" +
            "            \"type\": \"BOT\"\n" +
            "          }\n" +
            "        },\n" +
            "        \"type\": \"USER_MENTION\"\n" +
            "      }\n" +
            "    ],\n" +
            "  },\n" +
            "  \"user\": {\n" +
            "    \"name\": \"users/12345678901234567890\",\n" +
            "    \"displayName\": \"Chuck Norris\",\n" +
            "    \"avatarUrl\": \"https://lh3.googleusercontent.com/.../photo.jpg\",\n" +
            "    \"email\": \"chuck@example.com\"\n" +
            "  }\n" +
            "}";
}
