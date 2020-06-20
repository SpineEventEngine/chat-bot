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
     * Registers {@link com.google.pubsub.v1.PubsubPushNotification push notification}
     * Jackson deserializer.
     */
    @Singleton
    @Bean
    PubsubPushNotificationDeserializer pubsubDeserializer() {
        return new PubsubPushNotificationDeserializer();
    }

    /**
     * Spine-based {@link PubsubPushNotification} Jackson deserializer.
     */
    private static final class PubsubPushNotificationDeserializer
            extends JsonDeserializer<PubsubPushNotification> {

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
}
