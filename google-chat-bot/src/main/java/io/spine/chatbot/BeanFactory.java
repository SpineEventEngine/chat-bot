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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PubsubPushNotification;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.spine.json.Json;

import javax.inject.Singleton;
import java.io.IOException;

/**
 * Creates Micronaut context bean definitions.
 */
@Factory
final class BeanFactory {

    /**
     * Registers {@link PubsubPushNotification push notification} Jackson deserializer.
     */
    @Singleton
    @Bean
    PubsubPushNotificationDeserializer pubsubDeserializer() {
        return new PubsubPushNotificationDeserializer();
    }

    /**
     * Spine-based {@link PubsubPushNotification} Jackson deserializer.
     *
     * @see <a href="https://github.com/FasterXML/jackson-databind/wiki/Deserialization-Features">
     *         Jackson Deserialization</a>
     */
    @VisibleForTesting
    static final class PubsubPushNotificationDeserializer
            extends JsonDeserializer<PubsubPushNotification> {

        /**
         * Deserializes {@link PubsubPushNotification} JSON string into a Protobuf message.
         *
         * <p>While Protobuf JSON parser is not able to handle same fields that are set using
         * {@code lowerCamelCase} and {@code snake_case} notations, we manually drop duplicate
         * fields.
         *
         * @see <a href="https://github.com/protocolbuffers/protobuf/issues/7641">
         *         JsonFormat fails to parse JSON with both `lowerCamelCase` and `snake_case`
         *         fields</a>
         */
        @Override
        public PubsubPushNotification deserialize(JsonParser parser, DeserializationContext ctxt) {
            try {
                var jsonNode = ctxt.readTree(parser);
                var messageNode = (ObjectNode) jsonNode.get("message");
                messageNode.remove("message_id");
                messageNode.remove("publish_time");
                var pubsubMessage = Json.fromJson(messageNode.toString(), PubsubMessage.class);
                var subscription = jsonNode.get("subscription")
                                           .asText();
                var result = PubsubPushNotification
                        .newBuilder()
                        .setMessage(pubsubMessage)
                        .setSubscription(subscription)
                        .vBuild();
                return result;
            } catch (IOException e) {
                throw new RuntimeException("Unable to deserialize PubsubPushNotification json.", e);
            }
        }
    }
}
