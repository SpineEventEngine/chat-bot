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

package io.spine.chatbot.api.google.secret;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

import java.io.IOException;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Utility for accessing application secrets stored in Google Secret Manager.
 */
public final class Secrets {

    @SuppressWarnings("CallToSystemGetenv")
    private static final String PROJECT_ID = System.getenv("GCP_PROJECT_ID");
    private static final String TRAVIS_API_TOKEN = "TravisApiToken";
    private static final String CHAT_SERVICE_ACCOUNT = "ChatServiceAccount";

    /**
     * Prevents direct instantiation of the utility class.
     */
    private Secrets() {
    }

    /**
     * Retrieves the Travis CI API access token.
     */
    public static String travisToken() {
        var result = retrieveSecret(TRAVIS_API_TOKEN);
        return result;
    }

    /**
     * Retrieves the Google Chat API service account.
     */
    public static String chatServiceAccount() {
        var result = retrieveSecret(CHAT_SERVICE_ACCOUNT);
        return result;
    }

    private static String retrieveSecret(String secretName) {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            var secretVersion = SecretVersionName.of(PROJECT_ID, secretName, "latest");
            var secret = client.accessSecretVersion(secretVersion)
                               .getPayload()
                               .getData()
                               .toStringUtf8();
            return secret;
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to retrieve secret `%s`.", secretName);
        }
    }
}
