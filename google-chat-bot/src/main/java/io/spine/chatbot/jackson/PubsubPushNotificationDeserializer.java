package io.spine.chatbot.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.pubsub.v1.PubsubPushNotification;
import io.spine.json.Json;

import java.io.IOException;

/**
 * Spine-based {@link PubsubPushNotification} Jackson deserializer.
 */
public final class PubsubPushNotificationDeserializer extends JsonDeserializer<PubsubPushNotification> {

    /**
     * Deserializes {@link PubsubPushNotification} JSON string into a Protobuf message.
     */
    @Override
    public PubsubPushNotification deserialize(JsonParser parser, DeserializationContext ctxt) {
        try {
            var protoJson = parser.readValueAsTree()
                                  .toString();
            var result = Json.fromJson(protoJson, PubsubPushNotification.class);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize PubsubPushNotification json.", e);
        }
    }
}
