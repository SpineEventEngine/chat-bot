package io.spine.chatbot;

import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import com.google.pubsub.v1.PubsubPushNotification;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.spine.chatbot.google.chat.incoming.ChatEvent;
import io.spine.json.Json;
import io.spine.logging.Logging;

import java.util.Base64;

/**
 * A REST controller for handling incoming PubSub events from Google Chat users.
 */
@Controller("/chat")
public class IncomingEventsController implements Logging {

    /** Requests build status checks for registered listRepositories. **/
    @Post(value = "/incoming/event", consumes = MediaType.APPLICATION_JSON)
    public String checkRepositoryStatuses(@Body PubsubPushNotification pushNotification) {
        var message = pushNotification.getMessage();
        var chatEventJson = decodeBase64Json(message.getData());
        ChatEvent chatEvent = Json.fromJson(chatEventJson, ChatEvent.class);
        _debug().log("Received a new Chat event: %s", chatEvent);
        return "OK";
    }

    private static String decodeBase64Json(ByteString encoded) {
        var decodedBytes = Base64.getDecoder()
                                 .decode(encoded.toByteArray());
        return new String(decodedBytes);
    }
}
