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

package io.spine.chatbot.google.chat;

import com.google.api.services.chat.v1.model.Message;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.repository.build.Build;
import io.spine.chatbot.github.repository.build.Commit;
import io.spine.chatbot.google.chat.thread.ThreadResource;
import io.spine.chatbot.net.MoreUrls;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.chatbot.github.GitHubIdentifiers.repository;
import static io.spine.chatbot.github.Slugs.repoSlug;
import static io.spine.chatbot.google.chat.BuildStateUpdates.CANCELED_ICON;
import static io.spine.chatbot.google.chat.BuildStateUpdates.FAILURE_ICON;
import static io.spine.chatbot.google.chat.BuildStateUpdates.SUCCESS_ICON;
import static io.spine.chatbot.google.chat.BuildStateUpdates.buildStateMessage;
import static io.spine.chatbot.google.chat.GoogleChatIdentifiers.space;
import static io.spine.chatbot.net.MoreUrls.travisBuildUrlFor;

@DisplayName("`BuildStateUpdates` should")
final class BuildStateUpdatesTest extends UtilityClassTest<BuildStateUpdates> {

    private static final RepositoryId repo = repository("SpineEventEngine/publisher");
    private static final SpaceId space = space("spaces/pojwqdpo12");

    BuildStateUpdatesTest() {
        super(BuildStateUpdates.class);
    }

    @Nested
    @DisplayName("build a state update message")
    class BuildStateMessage {

        @Test
        @DisplayName("for a failed build")
        void failed() {
            var build = withState(Build.State.FAILED);
            var message = buildStateMessage(build, ThreadResource.getDefaultInstance());
            assertMessageWithIcon(build, message, FAILURE_ICON);
        }

        @Test
        @DisplayName("for a successful build")
        void success() {
            var build = withState(Build.State.PASSED);
            var message = buildStateMessage(build, ThreadResource.getDefaultInstance());
            assertMessageWithIcon(build, message, SUCCESS_ICON);
        }

        @Test
        @DisplayName("for a canceled build")
        void canceled() {
            var build = withState(Build.State.CANCELED);
            var message = buildStateMessage(build, ThreadResource.getDefaultInstance());
            assertMessageWithIcon(build, message, CANCELED_ICON);
        }

        private static void assertMessageWithIcon(Build build, Message message, String icon) {
            assertThat(message.getCards())
                    .hasSize(1);
            assertThat(message.getThread())
                    .isNull();
            var card = message.getCards()
                              .get(0);
            var header = card.getHeader();
            assertThat(header.getTitle())
                    .isEqualTo(build.getRepository()
                                    .getValue());
            assertThat(header.getImageUrl())
                    .isEqualTo(icon);
            var sections = card.getSections();
            assertThat(sections)
                    .hasSize(3);
        }

        private static Build withState(Build.State state) {
            var buildNumber = 441;
            var slug = repoSlug(repo);
            var commit = Commit.newBuilder()
                    .setSha("d5ce2b19fbda14a25deac948154722f33efd37b369a32be8f03ec2be8ef7d3a5")
                    .setAuthoredBy("Some guy")
                    .setMessage("Just a small change. Never mind.")
                    .setCommittedAt("2021-09-05T22:42")
                    .setCompareUrl(MoreUrls.githubUrlFor(slug))
                    .build();
            return Build.newBuilder()
                    .setRepository(slug)
                    .setNumber(String.valueOf(buildNumber))
                    .setLastCommit(commit)
                    .setTravisCiUrl(travisBuildUrlFor(slug, buildNumber))
                    .setSpace(space)
                    .setState(state)
                    .vBuild();
        }
    }
}
