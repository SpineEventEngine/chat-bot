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

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.chatbot.github.repository.RepositoryAware;
import io.spine.chatbot.github.repository.build.event.BuildCanceled;
import io.spine.chatbot.github.repository.build.event.BuildFailed;
import io.spine.chatbot.github.repository.build.event.BuildRecovered;
import io.spine.chatbot.google.chat.GoogleChatClient;
import io.spine.chatbot.google.chat.ThreadId;
import io.spine.chatbot.google.chat.thread.ThreadChat;
import io.spine.core.EventContext;
import io.spine.server.procman.ProcessManagerRepository;
import io.spine.server.route.EventRouting;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.thread;

/**
 * The repository for {@link ThreadChatProcess}es.
 */
final class ThreadChatRepository
        extends ProcessManagerRepository<ThreadId, ThreadChatProcess, ThreadChat> {

    private final GoogleChatClient client;

    ThreadChatRepository(GoogleChatClient client) {
        super();
        this.client = checkNotNull(client);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void setupEventRouting(EventRouting<ThreadId> routing) {
        super.setupEventRouting(routing);
        routing.unicast(BuildFailed.class, ThreadChatRepository::route)
               .unicast(BuildRecovered.class, ThreadChatRepository::route)
               .unicast(BuildCanceled.class, ThreadChatRepository::route);
    }

    private static ThreadId route(
            RepositoryAware e,
            @SuppressWarnings("unused" /* Required to avoid ambiguous routing. */) EventContext c
    ) {
        var repository = e.repository();
        return thread(repository.getValue());
    }

    @Override
    protected void configure(ThreadChatProcess processManager) {
        processManager.setClient(client);
    }
}
