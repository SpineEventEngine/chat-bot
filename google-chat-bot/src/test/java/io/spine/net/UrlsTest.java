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

package io.spine.net;

import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.net.Urls.githubRepoUrlFor;
import static io.spine.net.Urls.travisBuildUrlFor;
import static io.spine.net.Urls.travisRepoUrlFor;
import static io.spine.net.Urls.urlOfSpec;

@DisplayName("Urls should")
final class UrlsTest extends UtilityClassTest<Urls> {

    private static final String REPO_SLUG = "SpineEventEngine/chat-bot";

    UrlsTest() {
        super(Urls.class);
    }

    @DisplayName("compose URL for")
    @Nested
    @SuppressWarnings("ClassCanBeStatic") // jUnit Jupiter cannot work with static classes
    final class Compose {

        @DisplayName("Travis CI repository page")
        @Test
        void travisRepo() {
            assertThat(travisRepoUrlFor(REPO_SLUG)).isEqualTo(urlOfSpec(
                    "https://travis-ci.com/github/SpineEventEngine/chat-bot"
            ));
        }

        @DisplayName("Travis CI repository build page")
        @Test
        void travisBuild() {
            assertThat(travisBuildUrlFor(REPO_SLUG, 331)).isEqualTo(urlOfSpec(
                    "https://travis-ci.com/github/SpineEventEngine/chat-bot/builds/331"
            ));
        }

        @DisplayName("GitHub repository page")
        @Test
        void githubRepo() {
            assertThat(githubRepoUrlFor(REPO_SLUG)).isEqualTo(urlOfSpec(
                    "https://github.com/SpineEventEngine/chat-bot"
            ));
        }
    }
}
