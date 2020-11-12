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

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.chatbot.google.chat.InMemoryGoogleChatClient;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.AfterEach;

/**
 * An abstract test-base for Google Chat context-based tests.
 */
abstract class GoogleChatContextAwareTest extends ContextAwareTest {

    private final InMemoryGoogleChatClient client = InMemoryGoogleChatClient.strictClient();

    @Override
    protected final BoundedContextBuilder contextBuilder() {
        return GoogleChatContext
                .newBuilder()
                .setClient(client)
                .build()
                .builder();
    }

    @AfterEach
    @OverridingMethodsMustInvokeSuper
    @Override
    protected final void closeContext() {
        super.closeContext();
        client.reset();
    }

    /**
     * Returns configured for the {@link #context() context} Google Chat client.
     */
    final InMemoryGoogleChatClient googleChatClient() {
        return client;
    }
}
