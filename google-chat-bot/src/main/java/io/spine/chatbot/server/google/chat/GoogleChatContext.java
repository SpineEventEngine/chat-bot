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

package io.spine.chatbot.server.google.chat;

import io.spine.chatbot.google.chat.GoogleChatClient;
import io.spine.chatbot.server.ContextBuilderAware;
import io.spine.chatbot.server.DiagnosticEventLogger;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides {@link BoundedContextBuilder} for the Google Chat context.
 */
public final class GoogleChatContext implements ContextBuilderAware {

    /**
     * The name of the Google Chat Context.
     */
    static final String GOOGLE_CHAT_CONTEXT_NAME = "GoogleChat";

    private final BoundedContextBuilder builder;

    private GoogleChatContext(GoogleChatClient client) {
        this.builder = configureBuilder(checkNotNull(client));
    }

    /**
     * Returns the context builder associated with the Google Chat context.
     */
    @Override
    public BoundedContextBuilder builder() {
        return this.builder;
    }

    /**
     * Creates a new instance of the Google Chat context builder.
     */
    private static BoundedContextBuilder
    configureBuilder(GoogleChatClient client) {
        return BoundedContext
                .singleTenant(GOOGLE_CHAT_CONTEXT_NAME)
                .add(new SpaceRepository())
                .add(new ThreadRepository())
                .add(new ThreadChatRepository(client))
                .addEventDispatcher(new IncomingEventsHandler())
                .addEventDispatcher(new DiagnosticEventLogger());
    }

    /**
     * Creates a new builder of the Google Chat context.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a new Google Chat context.
     */
    public static GoogleChatContext newInstance() {
        return newBuilder().build();
    }

    /**
     * A Builder for configuring Google Chat context.
     */
    public static final class Builder {

        private @MonotonicNonNull GoogleChatClient client;

        /**
         * Prevents direct instantiation.
         */
        private Builder() {
        }

        /**
         * Sets Google Chat client to be used within the context.
         */
        public Builder setClient(GoogleChatClient client) {
            this.client = checkNotNull(client);
            return this;
        }

        /**
         * Finishes configuration of the context and builds a new instance.
         */
        public GoogleChatContext build() {
            if (client == null) {
                client = GoogleChatClient.newInstance();
            }
            return new GoogleChatContext(client);
        }
    }
}
