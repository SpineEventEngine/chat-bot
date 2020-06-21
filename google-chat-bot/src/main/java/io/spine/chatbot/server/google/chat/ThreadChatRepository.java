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

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.chatbot.api.google.chat.GoogleChatClient;
import io.spine.chatbot.github.repository.build.event.BuildFailed;
import io.spine.chatbot.github.repository.build.event.BuildRecovered;
import io.spine.chatbot.google.chat.ThreadId;
import io.spine.chatbot.google.chat.thread.ThreadChat;
import io.spine.chatbot.server.github.RepositoryAwareEvent;
import io.spine.core.EventContext;
import io.spine.server.procman.ProcessManagerRepository;
import io.spine.server.route.EventRoute;
import io.spine.server.route.EventRouting;

import java.util.Set;

import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.threadIdOf;
import static io.spine.server.route.EventRoute.withId;

/**
 * The repository for {@link ThreadChatProcess}es.
 */
final class ThreadChatRepository extends ProcessManagerRepository<ThreadId, ThreadChatProcess, ThreadChat> {

    private final GoogleChatClient googleChatClient;

    ThreadChatRepository(GoogleChatClient googleChatClient) {
        this.googleChatClient = googleChatClient;
    }

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void setupEventRouting(EventRouting<ThreadId> routing) {
        super.setupEventRouting(routing);
        routing.route(BuildFailed.class, new RepositoryEventRoute<>());
        routing.route(BuildRecovered.class, new RepositoryEventRoute<>());
    }

    @Override
    protected void configure(ThreadChatProcess processManager) {
        processManager.setGoogleChatClient(googleChatClient);
    }

    private static class RepositoryEventRoute<M extends RepositoryAwareEvent> implements EventRoute<ThreadId, M> {

        private static final long serialVersionUID = 5147803958347083018L;

        @Override
        public Set<ThreadId> apply(M event, EventContext context) {
            var repositoryId = event.getId();
            return withId(threadIdOf(repositoryId.getValue()));
        }
    }
}
