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

import com.google.api.services.chat.v1.model.Message;
import com.google.api.services.chat.v1.model.Thread;
import io.spine.base.EventMessage;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.repository.build.BuildState;
import io.spine.chatbot.github.repository.build.BuildStateChange;
import io.spine.chatbot.github.repository.build.event.BuildFailed;
import io.spine.chatbot.github.repository.build.event.BuildRecovered;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.ThreadId;
import io.spine.chatbot.google.chat.event.MessageCreated;
import io.spine.chatbot.google.chat.event.ThreadCreated;
import io.spine.chatbot.google.chat.thread.ThreadChat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.server.github.GitHubIdentifier.repositoryIdOf;
import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.messageIdOf;
import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.spaceIdOf;
import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.threadIdOf;
import static io.spine.chatbot.server.google.chat.ThreadResources.threadResourceOf;

@DisplayName("ThreadChatProcess should")
final class ThreadChatProcessTest {

    @SuppressWarnings("ClassCanBeStatic") // nested tests do not work with static classes
    @Nested
    @DisplayName("sent a message to the Google Chat room when build failed")
    final class BuildIsFailed extends BuildStateChanged {

        @Override
        EventMessage buildStateChangeEvent(RepositoryId repositoryId,
                                           BuildStateChange stateChange) {
            return BuildFailed
                    .newBuilder()
                    .setId(repositoryId)
                    .setChange(stateChange)
                    .vBuild();
        }
    }

    @SuppressWarnings("ClassCanBeStatic") // nested tests do not work with static classes
    @Nested
    @DisplayName("sent a message to the Google Chat room when build recovered from failure")
    final class BuildIsRecovered extends BuildStateChanged {

        @Override
        EventMessage buildStateChangeEvent(RepositoryId repositoryId,
                                           BuildStateChange stateChange) {
            return BuildRecovered
                    .newBuilder()
                    .setId(repositoryId)
                    .setChange(stateChange)
                    .vBuild();
        }
    }

    private abstract static class BuildStateChanged extends GoogleChatContextAwareTest {

        private final RepositoryId repositoryId = repositoryIdOf("SpineEventEngine/money");
        private final ThreadId threadId = threadIdOf(repositoryId.getValue());
        private final String googleChatSpace = "spaces/1241pjwqe";
        private final SpaceId spaceId = spaceIdOf(googleChatSpace);
        private final String buildNumber = "551";
        private final Thread newThread = new Thread().setName("spaces/1241pjwqe/threads/k12d1o2r1");
        private final Message sentMessage = new Message()
                .setName("spaces/1241pjwqe/messages/12154363643624")
                .setThread(newThread);

        @BeforeEach
        void setUp() {
            googleChatClient().setMessageForBuildStatusUpdate(buildNumber, sentMessage);
            var newBuildState = BuildState
                    .newBuilder()
                    .setGoogleChatSpace(googleChatSpace)
                    .setNumber(buildNumber)
                    .vBuild();
            var buildStateChange = BuildStateChange
                    .newBuilder()
                    .setNewValue(newBuildState)
                    .vBuild();
            var buildFailed = buildStateChangeEvent(repositoryId, buildStateChange);
            context().receivesExternalEvent(buildFailed);
        }

        abstract EventMessage buildStateChangeEvent(RepositoryId repositoryId,
                                                    BuildStateChange stateChange);

        @Test
        @DisplayName("producing MessageCreated and ThreadCreated events")
        void producingEvents() {
            var messageCreated = MessageCreated
                    .newBuilder()
                    .setId(messageIdOf(sentMessage.getName()))
                    .setThreadId(threadId)
                    .setSpaceId(spaceId)
                    .vBuild();
            var threadCreated = ThreadCreated
                    .newBuilder()
                    .setId(threadId)
                    .setThread(threadResourceOf(newThread.getName()))
                    .setSpaceId(spaceId)
                    .vBuild();
            context().assertEvent(messageCreated);
            context().assertEvent(threadCreated);
        }

        @Test
        @DisplayName("setting process state")
        void settingState() {
            var expectedState = ThreadChat
                    .newBuilder()
                    .setId(threadId)
                    .setSpaceId(spaceId)
                    .setThread(threadResourceOf(newThread.getName()))
                    .vBuild();
            context().assertState(threadId, ThreadChat.class)
                     .isEqualTo(expectedState);
        }
    }
}
