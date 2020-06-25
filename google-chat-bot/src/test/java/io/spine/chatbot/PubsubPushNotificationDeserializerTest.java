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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.truth.extensions.proto.ProtoTruth;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.Timestamps;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.jackson.ObjectMapperFactory;
import io.micronaut.test.annotation.MicronautTest;
import io.spine.pubsub.PubsubPushRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.text.ParseException;

@MicronautTest
@DisplayName("PubsubPushNotificationDeserializer should")
final class PubsubPushNotificationDeserializerTest {

    @Inject
    private ObjectMapperFactory mapperFactory;

    @Test
    @DisplayName("deserialize Pubsub message")
    void deserializePubsubMessage() throws JsonProcessingException, ParseException {

        var pubsubMessage = PubsubMessage
                .newBuilder()
                .setMessageId("450292511223766")
                .setPublishTime(Timestamps.parse("2020-06-21T20:48:25.908Z"))
                .setData(ByteString.copyFromUtf8("{\"key\":\"value\"}"))
                .buildPartial();
        var expectedResult = PubsubPushRequest
                .newBuilder()
                .setSubscription("projects/test-project/subscriptions/test-subscription")
                .setMessage(pubsubMessage)
                .vBuild();

        var mapper = mapperFactory.objectMapper(null, null);

        var pushNotification = mapper.readValue(PUSH_NOTIFICATION_JSON,
                                                PubsubPushRequest.class);
        ProtoTruth.assertThat(pushNotification)
                  .isEqualTo(expectedResult);
    }

    private static final String PUSH_NOTIFICATION_JSON = "" +
            "{\n" +
            "  \"message\": {\n" +
            "    \"data\": \"eyJrZXkiOiJ2YWx1ZSJ9\",\n" +
            "    \"messageId\": \"450292511223766\",\n" +
            "    \"message_id\": \"450292511223766\",\n" +
            "    \"publishTime\": \"2020-06-21T20:48:25.908Z\",\n" +
            "    \"publish_time\": \"2020-06-21T20:48:25.908Z\"\n" +
            "  },\n" +
            "  \"subscription\": \"projects/test-project/subscriptions/test-subscription\"\n" +
            "}";
}
