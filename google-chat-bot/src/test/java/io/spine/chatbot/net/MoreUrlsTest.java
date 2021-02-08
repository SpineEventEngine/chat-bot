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

package io.spine.chatbot.net;

import io.spine.chatbot.github.Slug;
import io.spine.chatbot.github.Slugs;
import io.spine.net.Urls;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.chatbot.net.MoreUrls.githubUrlFor;
import static io.spine.chatbot.net.MoreUrls.travisBuildUrlFor;
import static io.spine.chatbot.net.MoreUrls.travisUrlFor;

@DisplayName("`MoreUrls` should")
final class MoreUrlsTest extends UtilityClassTest<MoreUrls> {

    private static final Slug REPO_SLUG = Slugs.newSlug("SpineEventEngine/chat-bot");

    MoreUrlsTest() {
        super(MoreUrls.class);
    }

    @DisplayName("compose URL for")
    @Nested
    @SuppressWarnings("ClassCanBeStatic") // jUnit Jupiter cannot work with static classes
    final class Compose {

        @DisplayName("Travis CI repository page")
        @Test
        void travisRepo() {
            assertThat(travisUrlFor(REPO_SLUG)).isEqualTo(Urls.create(
                    "https://travis-ci.com/github/SpineEventEngine/chat-bot"
            ));
        }

        @DisplayName("Travis CI repository build page")
        @Test
        void travisBuild() {
            assertThat(travisBuildUrlFor(REPO_SLUG, 331)).isEqualTo(Urls.create(
                    "https://travis-ci.com/github/SpineEventEngine/chat-bot/builds/331"
            ));
        }

        @DisplayName("GitHub repository page")
        @Test
        void githubRepo() {
            assertThat(githubUrlFor(REPO_SLUG)).isEqualTo(Urls.create(
                    "https://github.com/SpineEventEngine/chat-bot"
            ));
        }
    }
}
