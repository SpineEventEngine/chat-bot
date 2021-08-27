/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.Resources;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.Timestamps;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.jackson.ObjectMapperFactory;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.spine.pubsub.PubsubPushRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.testing.TestValues.nullRef;
import static io.spine.util.Exceptions.newIllegalStateException;

@MicronautTest
@DisplayName("`PubsubPushRequestDeserializer` should")
final class PubsubPushRequestDeserializerTest {

    @Inject
    private ObjectMapperFactory mapperFactory;

    @Test
    @DisplayName("deserialize Pub/Sub message")
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

        var mapper = mapperFactory.objectMapper(nullRef(), nullRef());

        var pushRequest = mapper.readValue(pushRequestJson(), PubsubPushRequest.class);
        assertThat(pushRequest)
                .isEqualTo(expectedResult);
    }

    private static String pushRequestJson() {
        try {
            var resource = Resources.getResource("pubsub_push_request.json");
            return Resources.toString(resource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Unable to load PubsubPushRequest message JSON definition."
            );
        }
    }
}
