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

package io.spine.chatbot.server.google.chat;

import io.spine.chatbot.google.chat.GoogleChatIdentifiers;
import io.spine.chatbot.google.chat.MessageId;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.testing.UtilityClassTest;
import io.spine.validate.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.message;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.space;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@DisplayName("`GoogleChatIdentifiers` should")
final class GoogleChatIdentifiersTest extends UtilityClassTest<GoogleChatIdentifiers> {

    GoogleChatIdentifiersTest() {
        super(GoogleChatIdentifiers.class);
    }

    // nested tests do not work with static classes
    @SuppressWarnings({"ClassCanBeStatic", "ResultOfMethodCallIgnored"})
    @TestInstance(PER_CLASS)
    @Nested
    @DisplayName("not accept invalid")
    final class NotAcceptInvalid {

        @ParameterizedTest
        @MethodSource("spaceIdsSource")
        @DisplayName("space IDs")
        void spaceIds(String value) {
            assertThrows(ValidationException.class, () -> space(value));
        }

        @ParameterizedTest
        @MethodSource("messageIdsSource")
        @DisplayName("space IDs")
        void messageIds(String value) {
            assertThrows(ValidationException.class, () -> message(value));
        }

        @SuppressWarnings("unused") // method is used as parameterized test source
        private Stream<String> spaceIdsSource() {
            return Stream.of("spaces/", "spacs/12415");
        }

        @SuppressWarnings("unused") // method is used as parameterized test source
        private Stream<String> messageIdsSource() {
            return Stream.of("spaces/", "spaces/qwe124", "spaces/eqwt23/messages/");
        }
    }

    @Test
    @DisplayName("create space ID")
    void createSpaceId() {
        var spaceId = "spaces/qew21466";
        var expectedSpace = SpaceId
                .newBuilder()
                .setValue(spaceId)
                .buildPartial();
        assertThat(space(spaceId))
                .isEqualTo(expectedSpace);
    }

    @Test
    @DisplayName("create message ID")
    void createMessageId() {
        var messageId = "spaces/qew21466/messages/123112111";
        var expectedMessage = MessageId
                .newBuilder()
                .setValue(messageId)
                .buildPartial();
        assertThat(message(messageId))
                .isEqualTo(expectedMessage);
    }
}
