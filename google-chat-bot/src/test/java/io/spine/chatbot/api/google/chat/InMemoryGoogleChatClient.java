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

import com.google.api.services.chat.v1.model.Message;
import io.spine.chatbot.api.FailFastAwareClient;
import io.spine.chatbot.github.repository.build.Build;
import io.spine.chatbot.google.chat.thread.ThreadResource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An in-memory test-only implementation of the Google Chat client.
 */
public final class InMemoryGoogleChatClient extends FailFastAwareClient implements GoogleChatClient {

    private final Map<String, Message> sentMessages = new ConcurrentHashMap<>();

    private InMemoryGoogleChatClient(boolean failFast) {
        super(failFast);
    }

    /**
     * Creates a {@link #failFast} in-memory Google Chat client.
     */
    public static InMemoryGoogleChatClient strictClient() {
        return new InMemoryGoogleChatClient(true);
    }

    /**
     * Creates a lenient in-memory Google Chat client.
     */
    public static InMemoryGoogleChatClient lenientClient() {
        return new InMemoryGoogleChatClient(false);
    }

    @Override
    public Message sendBuildStateUpdate(Build buildState, ThreadResource thread) {
        var stubbedValue = sentMessages.get(buildState.getNumber());
        var result = failOrDefault(stubbedValue, buildState.getNumber(), new Message());
        return result;
    }

    /**
     * Sets up a stub {@code message} for a build state update with the specified
     * {@code buildNumber}.
     */
    public void setMessageForBuildStatusUpdate(String buildNumber, Message message) {
        checkNotNull(buildNumber);
        checkNotNull(message);
        sentMessages.put(buildNumber, message);
    }

    /**
     * Resets state of the configured responses.
     */
    public void reset() {
        sentMessages.clear();
    }
}
