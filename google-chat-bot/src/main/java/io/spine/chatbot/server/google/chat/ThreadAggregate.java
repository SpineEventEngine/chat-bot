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

import io.spine.chatbot.google.chat.ThreadId;
import io.spine.chatbot.google.chat.event.MessageCreated;
import io.spine.chatbot.google.chat.event.ThreadCreated;
import io.spine.chatbot.google.chat.thread.Thread;
import io.spine.chatbot.google.chat.thread.event.MessageAdded;
import io.spine.chatbot.google.chat.thread.event.ThreadInitialized;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.event.React;

final class ThreadAggregate extends Aggregate<ThreadId, Thread, Thread.Builder> {

    @React
    ThreadInitialized on(ThreadCreated e) {
        return ThreadInitialized
                .newBuilder()
                .setId(e.getId())
                .setThread(e.getThread())
                .setSpaceId(e.getSpaceId())
                .vBuild();
    }

    @Apply
    private void on(ThreadInitialized e) {
        builder().setThread(e.getThread())
                 .setSpaceId(e.getSpaceId());
    }

    @React
    MessageAdded on(MessageCreated e) {
        return MessageAdded
                .newBuilder()
                .setId(e.getId())
                .setThreadId(e.getThreadId())
                .vBuild();
    }

    @Apply
    private void on(MessageAdded e) {
        builder().addMessages(e.getId());
    }
}
