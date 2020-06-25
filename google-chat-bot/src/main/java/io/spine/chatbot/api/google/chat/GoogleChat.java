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

package io.spine.chatbot.api.google.chat;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.chat.v1.HangoutsChat;
import com.google.api.services.chat.v1.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.chatbot.api.google.secret.Secrets;
import io.spine.chatbot.github.repository.build.BuildState;
import io.spine.chatbot.google.chat.thread.ThreadResource;
import io.spine.logging.Logging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

import static io.spine.chatbot.api.google.chat.BuildStateUpdates.buildStateMessage;

/**
 * Google Chat Hangouts API client.
 *
 * @see <a href="https://developers.google.com/hangouts/chat/concepts">Hangouts Chat API</a>
 */
public final class GoogleChat implements GoogleChatClient, Logging {

    private static final String BOT_NAME = "Spine Chat Bot";
    private static final String CHAT_BOT_SCOPE = "https://www.googleapis.com/auth/chat.bot";

    private final HangoutsChat hangoutsChat;

    private GoogleChat(HangoutsChat chat) {
        hangoutsChat = chat;
    }

    /**
     * Creates a new Google Chat client.
     *
     * <p>The client is backed by {@link HangoutsChat} API.
     */
    public static GoogleChatClient newInstance() {
        return new GoogleChat(hangoutsChat());
    }

    @Override
    public Message sendBuildStateUpdate(BuildState buildState, ThreadResource thread) {
        var repoSlug = buildState.getRepositorySlug();
        var debug = _debug();
        debug.log("Building state update message for repository `%s`.", repoSlug);
        var message = buildStateMessage(buildState, thread);
        debug.log("Sending state update message for repository `%s`.", repoSlug);
        var result = sendMessage(buildState.getGoogleChatSpace(), message);
        debug.log(
                "Build state update message with ID `%s` for repository `%s` sent to thread `%s`.",
                result.getName(), repoSlug, result.getThread()
                                                  .getName()
        );
        return result;
    }

    @CanIgnoreReturnValue
    private Message sendMessage(String space, Message message) {
        try {
            return hangoutsChat
                    .spaces()
                    .messages()
                    .create(space, message)
                    .execute();
        } catch (IOException e) {
            logger().atSevere()
                    .withCause(e)
                    .log("Unable to send message to space `%s`.", space);
            throw new RuntimeException("Unable to send message to space " + space, e);
        }
    }

    private static HangoutsChat hangoutsChat() {
        try {
            var serviceAccount = Secrets.chatServiceAccount();
            var credentials = GoogleCredentials.fromStream(streamFrom(serviceAccount))
                                               .createScoped(CHAT_BOT_SCOPE);
            var credentialsAdapter = new HttpCredentialsAdapter(credentials);
            var chat = new HangoutsChat.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credentialsAdapter)
                    .setApplicationName(BOT_NAME)
                    .build();
            return chat;
        } catch (IOException | GeneralSecurityException e) {
            String message = "Unable to create Hangouts Chat client.";
            Logging.loggerFor(GoogleChat.class)
                   .atSevere()
                   .withCause(e)
                   .log(message);
            throw new RuntimeException(message, e);
        }
    }

    private static InputStream streamFrom(String data) {
        return new ByteArrayInputStream(data.getBytes(Charset.defaultCharset()));
    }
}
