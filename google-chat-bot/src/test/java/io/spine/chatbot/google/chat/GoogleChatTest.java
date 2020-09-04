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

package io.spine.chatbot.google.chat;

import com.google.api.client.googleapis.testing.auth.oauth2.MockGoogleCredential;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.json.MockJsonFactory;
import com.google.api.services.chat.v1.HangoutsChat;
import com.google.api.services.chat.v1.model.Message;
import com.google.api.services.chat.v1.model.Thread;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.Slug;
import io.spine.chatbot.github.repository.build.Build;
import io.spine.chatbot.github.repository.build.Commit;
import io.spine.chatbot.google.chat.thread.ThreadResource;
import io.spine.chatbot.net.MoreUrls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.chatbot.github.GitHubIdentifiers.repository;
import static io.spine.chatbot.github.Slugs.repoSlug;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.message;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.space;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.thread;
import static io.spine.chatbot.net.MoreUrls.travisBuildUrlFor;
import static io.spine.chatbot.server.google.chat.ThreadResources.threadResource;

@DisplayName("`GoogleChat` should send a message to")
final class GoogleChatTest {

    @DisplayName("send a message to")
    @Nested
    @SuppressWarnings("ClassCanBeStatic") // jUnit5 is not able to work with nested static class
    final class SendMessage {

        private final RepositoryId repo = repository("SpineEventEngine/publisher");
        private final Slug slug = repoSlug(repo);
        private final SpaceId space = space("spaces/pojwqdpo12");
        private final String threadName = space.getValue() + "/threads/qiwd124";
        private final String messageId = space.getValue() + "/messages/ojwqpj14";
        private Build build;

        @BeforeEach
        void prepareBuild() {
            var buildNumber = 441;
            var commit = Commit
                    .newBuilder()
                    .setSha("pd1hehr1i3oh213121")
                    .setAuthoredBy("Greatest Dev")
                    .setMessage("The commit that fixed everything.")
                    .setCommittedAt("2020-09-04T12:19")
                    .setCompareUrl(MoreUrls.githubUrlFor(slug))
                    .build();
            build = Build
                    .newBuilder()
                    .setRepository(slug)
                    .setNumber(String.valueOf(buildNumber))
                    .setState(Build.State.PASSED)
                    .setLastCommit(commit)
                    .setTravisCiUrl(travisBuildUrlFor(slug, buildNumber))
                    .setSpace(space)
                    .vBuild();
        }

        @Test
        @DisplayName("a new thread")
        void newThread() {
            var chatApi = new NoOpChat(
                    message -> message
                            .clone()
                            .setThread(new Thread().setName(threadName))
                            .setName(messageId)
            );
            var chat = new GoogleChat(chatApi);
            var actualUpdate =
                    chat.sendBuildStateUpdate(build, ThreadResource.getDefaultInstance());
            var expectedUpdate = BuildStateUpdate
                    .newBuilder()
                    .setMessage(message(messageId))
                    .setResource(threadResource(threadName))
                    .setThread(thread(repo.getValue()))
                    .setSpace(space)
                    .vBuild();
            assertThat(actualUpdate)
                    .comparingExpectedFieldsOnly()
                    .isEqualTo(expectedUpdate);
        }

        @Test
        @DisplayName("existing thread")
        void existingThread() {
            var chatApi = new NoOpChat(
                    message -> message
                            .clone()
                            .setName(messageId)
            );
            var chat = new GoogleChat(chatApi);
            var threadResource = threadResource(threadName);
            var actualUpdate =
                    chat.sendBuildStateUpdate(build, threadResource);
            var expectedUpdate = BuildStateUpdate
                    .newBuilder()
                    .setMessage(message(messageId))
                    .setResource(threadResource)
                    .setThread(thread(repo.getValue()))
                    .setSpace(space)
                    .vBuild();
            assertThat(actualUpdate)
                    .comparingExpectedFieldsOnly()
                    .isEqualTo(expectedUpdate);
        }
    }

    /**
     * A no-operation {@code HangoutsChat} stub.
     */
    @SuppressWarnings("InnerClassTooDeeplyNested")
    private static class NoOpChat extends HangoutsChat {

        private final ResponseMessageConverter responseConverter;

        private NoOpChat(ResponseMessageConverter responseConverter) {
            super(new MockHttpTransport(),
                  new MockJsonFactory(),
                  new MockGoogleCredential.Builder().build());
            this.responseConverter = responseConverter;
        }

        @Override
        public Spaces spaces() {
            return new Spaces() {

                @Override
                public Messages messages() {
                    return new Messages() {

                        @Override
                        public NoOpCreateMessages create(String parent, Message content) {
                            return new NoOpCreateMessages(parent, content);
                        }

                        class NoOpCreateMessages extends Create {

                            private final Message messageToSend;

                            private NoOpCreateMessages(String parent, Message content) {
                                super(parent, content);
                                messageToSend = content;
                            }

                            @Override
                            public Message execute() {
                                return responseConverter.convert(messageToSend);
                            }
                        }
                    };
                }
            };
        }

        @FunctionalInterface
        private interface ResponseMessageConverter {

            /**
             * Converts input {@code message} into a response.
             */
            Message convert(Message message);
        }
    }
}
