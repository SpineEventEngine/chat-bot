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

package io.spine.chatbot.google.chat;

import com.google.auth.oauth2.GoogleCredentials;
import io.spine.chatbot.google.secret.Secret;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A service account key configured for Google Chat.
 */
final class GoogleChatKey extends Secret {

    private static final String CHAT_BOT_SCOPE = "https://www.googleapis.com/auth/chat.bot";
    private static final String CHAT_SERVICE_ACCOUNT = "ChatServiceAccount";

    private final String value;

    private GoogleChatKey(String value) {
        this.value = value;
    }

    /**
     * Creates the Google Chat service account key.
     */
    static GoogleChatKey chatServiceAccountKey() {
        var value = checkNotEmptyOrBlank(retrieveSecret(CHAT_SERVICE_ACCOUNT));
        return new GoogleChatKey(value);
    }

    /**
     * Converts the key to respective scoped {@code GoogleCredentials}.
     */
    GoogleCredentials toCredentials() {
        try {
            return GoogleCredentials
                    .fromStream(streamFrom(value))
                    .createScoped(CHAT_BOT_SCOPE);
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to read `GoogleCredentials`.");
        }
    }

    private static InputStream streamFrom(String data) {
        return new ByteArrayInputStream(data.getBytes(Charset.defaultCharset()));
    }
}
