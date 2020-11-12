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

package io.spine.chatbot.google.chat;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.chat.v1.HangoutsChat;
import com.google.auth.http.HttpCredentialsAdapter;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static io.spine.chatbot.google.chat.GoogleChatKey.chatServiceAccountKey;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Provides fully-configured {@link HangoutsChat chat} client.
 */
final class HangoutsChatFactory {

    private static final String BOT_NAME = "Spine ChatBot";

    /**
     * Prevents direct instantiation of the utility class.
     */
    private HangoutsChatFactory() {
    }

    /**
     * Creates a new instance of the {@link HangoutsChat} client.
     */
    static HangoutsChat newInstance() {
        var credentials = chatServiceAccountKey().toCredentials();
        var credentialsAdapter = new HttpCredentialsAdapter(credentials);
        var chat = chatWithCredentials(credentialsAdapter)
                .setApplicationName(BOT_NAME)
                .build();
        return chat;
    }

    private static HangoutsChat.Builder
    chatWithCredentials(HttpCredentialsAdapter credentialsAdapter) {
        var transport = newTrustedTransport();
        var jacksonFactory = JacksonFactory.getDefaultInstance();
        return new HangoutsChat.Builder(transport, jacksonFactory, credentialsAdapter);
    }

    private static HttpTransport newTrustedTransport() {
        try {
            return GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw newIllegalStateException(e, "Unable to instantiate trusted transport.");
        }
    }
}
