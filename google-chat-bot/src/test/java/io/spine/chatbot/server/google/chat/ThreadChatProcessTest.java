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

import io.spine.base.EventMessage;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.repository.build.Build;
import io.spine.chatbot.github.repository.build.BuildStateChange;
import io.spine.chatbot.github.repository.build.event.BuildCanceled;
import io.spine.chatbot.github.repository.build.event.BuildFailed;
import io.spine.chatbot.github.repository.build.event.BuildRecovered;
import io.spine.chatbot.google.chat.BuildStateUpdate;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.ThreadId;
import io.spine.chatbot.google.chat.event.MessageCreated;
import io.spine.chatbot.google.chat.event.ThreadCreated;
import io.spine.chatbot.google.chat.thread.ThreadChat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.github.GitHubIdentifiers.repository;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.message;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.space;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.thread;
import static io.spine.chatbot.server.google.chat.ThreadResources.threadResource;

@DisplayName("`ThreadChatProcess` should")
final class ThreadChatProcessTest {

    @Nested
    @DisplayName("sent a message to the Google Chat room when build failed")
    @SuppressWarnings("ClassCanBeStatic" /* Nested tests do not work with static classes. */)
    final class BuildIsFailed extends BuildStateChanged {

        @Override
        BuildFailed buildStateChangeEvent(RepositoryId repo, BuildStateChange stateChange) {
            return BuildFailed.newBuilder()
                    .setRepository(repo)
                    .setChange(stateChange)
                    .vBuild();
        }
    }

    @Nested
    @DisplayName("sent a message to the Google Chat room when build is canceled")
    @SuppressWarnings("ClassCanBeStatic" /* Nested tests do not work with static classes. */)
    final class BuildIsCanceled extends BuildStateChanged {

        @Override
        BuildCanceled buildStateChangeEvent(RepositoryId repo, BuildStateChange stateChange) {
            return BuildCanceled.newBuilder()
                    .setRepository(repo)
                    .setChange(stateChange)
                    .vBuild();
        }
    }

    @Nested
    @DisplayName("sent a message to the Google Chat room when build recovered from failure")
    @SuppressWarnings("ClassCanBeStatic" /* Nested tests do not work with static classes. */)
    final class BuildIsRecovered extends BuildStateChanged {

        @Override
        BuildRecovered buildStateChangeEvent(RepositoryId repo, BuildStateChange stateChange) {
            return BuildRecovered.newBuilder()
                    .setRepository(repo)
                    .setChange(stateChange)
                    .vBuild();
        }
    }

    private abstract static class BuildStateChanged extends GoogleChatContextAwareTest {

        private static final String buildNumber = "551";

        private final RepositoryId repo = repository("SpineEventEngine/money");
        private final ThreadId thread = thread(repo.getValue());
        private final SpaceId space = space("spaces/1241pjwqe");

        private final BuildStateUpdate stateUpdate = BuildStateUpdate
                .newBuilder()
                .setSpace(space)
                .setMessage(message("spaces/1241pjwqe/messages/12154363643624"))
                .setThread(thread)
                .setResource(threadResource("spaces/1241pjwqe/threads/k12d1o2r1"))
                .vBuild();

        @BeforeEach
        void receiveBuildStateChange() {
            googleChatClient().setBuildStateUpdate(buildNumber, stateUpdate);
            var newBuildState = Build.newBuilder()
                    .setSpace(space)
                    .setNumber(buildNumber)
                    .vBuild();
            var buildStateChange = BuildStateChange.newBuilder()
                    .setNewValue(newBuildState)
                    .vBuild();
            var event = buildStateChangeEvent(repo, buildStateChange);
            context().receivesExternalEvent(event);
        }

        abstract EventMessage buildStateChangeEvent(RepositoryId repo, BuildStateChange change);

        @Test
        @DisplayName("producing `MessageCreated` and `ThreadCreated` events")
        void producingEvents() {
            var messageCreated = MessageCreated.newBuilder()
                    .setMessage(stateUpdate.getMessage())
                    .setThread(thread)
                    .setSpace(space)
                    .vBuild();
            var threadCreated = ThreadCreated.newBuilder()
                    .setThread(thread)
                    .setResource(stateUpdate.getResource())
                    .setSpace(space)
                    .vBuild();
            context().assertEvent(messageCreated);
            context().assertEvent(threadCreated);
        }

        @Test
        @DisplayName("setting process state")
        void settingState() {
            var expectedState = ThreadChat.newBuilder()
                    .setThread(thread)
                    .setSpace(space)
                    .setResource(stateUpdate.getResource())
                    .vBuild();
            context().assertState(thread, ThreadChat.class)
                     .isEqualTo(expectedState);
        }
    }
}
