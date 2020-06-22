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

package io.spine.chatbot.server.google.chat;

import io.spine.chatbot.api.google.chat.GoogleChat;
import io.spine.chatbot.api.google.chat.GoogleChatClient;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides {@link BoundedContextBuilder} for the Google Chat context.
 */
public final class GoogleChatContext {

    /** The name of the Google Chat Context. **/
    static final String GOOGLE_CHAT_CONTEXT_NAME = "GoogleChat";

    private final BoundedContextBuilder contextBuilder;

    private GoogleChatContext(GoogleChatClient googleChatClient) {
        this.contextBuilder = configureContextBuilder(googleChatClient);
    }

    /**
     * Returns the context builder associated with the Google Chat context.
     */
    public BoundedContextBuilder contextBuilder() {
        return this.contextBuilder;
    }

    /**
     * Creates a new instance of the Google Chat context builder.
     */
    private static BoundedContextBuilder
    configureContextBuilder(GoogleChatClient googleChatClient) {
        return BoundedContext
                .singleTenant(GOOGLE_CHAT_CONTEXT_NAME)
                .add(new SpaceRepository())
                .add(new ThreadRepository())
                .add(new ThreadChatRepository(googleChatClient))
                .addEventDispatcher(new IncomingEventsHandler());
    }

    /**
     * Creates a new builder of the Google Chat context.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A Builder for configuring Google Chat context.
     */
    public static final class Builder {

        private GoogleChatClient googleChatClient;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        /**
         * Sets Google Chat client to be used within the context.
         */
        public Builder setGoogleChatClient(GoogleChatClient googleChatClient) {
            checkNotNull(googleChatClient);
            this.googleChatClient = googleChatClient;
            return this;
        }

        /**
         * Finishes configuration of the context and builds a new instance.
         */
        public GoogleChatContext build() {
            if (googleChatClient == null) {
                googleChatClient = GoogleChat.newInstance();
            }
            return new GoogleChatContext(googleChatClient);
        }
    }
}
