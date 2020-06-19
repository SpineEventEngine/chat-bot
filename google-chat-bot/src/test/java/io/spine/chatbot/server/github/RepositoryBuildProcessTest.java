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

package io.spine.chatbot.server.github;

import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.repository.build.BuildState;
import io.spine.chatbot.github.repository.build.BuildStateChange;
import io.spine.chatbot.github.repository.build.RepositoryBuild;
import io.spine.chatbot.github.repository.build.command.CheckRepositoryBuild;
import io.spine.chatbot.github.repository.build.event.BuildFailed;
import io.spine.chatbot.github.repository.build.event.BuildRecovered;
import io.spine.chatbot.travis.Build;
import io.spine.chatbot.travis.BuildsResponse;
import io.spine.chatbot.travis.Commit;
import io.spine.chatbot.travis.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.server.github.Identifiers.repositoryIdOf;
import static io.spine.chatbot.server.github.RepositoryBuildProcess.buildStateFrom;

@DisplayName("RepositoryBuildProcess should")
final class RepositoryBuildProcessTest extends GitHubEntityTest {

    private static final RepositoryId repositoryId = repositoryIdOf("SpineEventEngine/web");

    @SuppressWarnings("ClassCanBeStatic") // nested tests do not work with static classes
    @Nested
    @DisplayName("handle build failure")
    final class FailedBuild {

        private final Build build = failedBuild();
        private final BuildState buildState = buildStateFrom(build);

        @BeforeEach
        void setUp() {
            travisClient.setBuildsFor(repositoryId.getValue(), singleBuild(build));
            var checkRepoBuild = CheckRepositoryBuild
                    .newBuilder()
                    .setId(repositoryId)
                    .vBuild();
            context().receivesCommand(checkRepoBuild);
        }

        @Test
        @DisplayName("producing BuildFailed event")
        void producingEvent() {
            var stateChange = BuildStateChange
                    .newBuilder()
                    .setNewValue(buildState)
                    .vBuild();
            var buildFailed = BuildFailed
                    .newBuilder()
                    .setId(repositoryId)
                    .setChange(stateChange)
                    .vBuild();
            context().assertEvent(buildFailed);
        }

        @Test
        @DisplayName("setting process state")
        void settingState() {
            var expectedState = RepositoryBuild
                    .newBuilder()
                    .setId(repositoryId)
                    .setBuildState(buildState)
                    .vBuild();
            context().assertState(repositoryId, RepositoryBuild.class)
                     .isEqualTo(expectedState);
        }
    }

    @SuppressWarnings("ClassCanBeStatic") // nested tests do not work with static classes
    @Nested
    @DisplayName("handle build recovery")
    final class RecoveredBuild {

        private final Build previousBuild = failedBuild();
        private final BuildState previousBuildState = buildStateFrom(previousBuild);

        private final Build newBuild = passingBuild();
        private final BuildState newBuildState = buildStateFrom(newBuild);

        @BeforeEach
        void setUp() {
            travisClient.setBuildsFor(repositoryId.getValue(), singleBuild(previousBuild));
            var checkRepoFailure = CheckRepositoryBuild
                    .newBuilder()
                    .setId(repositoryId)
                    .vBuild();
            context().receivesCommand(checkRepoFailure);
            travisClient.setBuildsFor(repositoryId.getValue(), singleBuild(newBuild));
            var checkRepoRecovery = CheckRepositoryBuild
                    .newBuilder()
                    .setId(repositoryId)
                    .vBuild();
            context().receivesCommand(checkRepoRecovery);
        }

        @Test
        @DisplayName("producing BuildRecovered event")
        void producingEvent() {
            var stateChange = BuildStateChange
                    .newBuilder()
                    .setPreviousValue(previousBuildState)
                    .setNewValue(newBuildState)
                    .vBuild();
            var buildFailed = BuildRecovered
                    .newBuilder()
                    .setId(repositoryId)
                    .setChange(stateChange)
                    .vBuild();
            context().assertEvent(buildFailed);
        }

        @Test
        @DisplayName("setting process state")
        void settingState() {
            var expectedState = RepositoryBuild
                    .newBuilder()
                    .setId(repositoryId)
                    .setBuildState(newBuildState)
                    .vBuild();
            context().assertState(repositoryId, RepositoryBuild.class)
                     .isEqualTo(expectedState);
        }
    }

    private static BuildsResponse singleBuild(Build build) {
        return BuildsResponse
                .newBuilder()
                .addBuilds(build)
                .buildPartial();
    }

    private static Build passingBuild() {
        return Build
                .newBuilder()
                .setId(123153L)
                .setNumber("42")
                .setState("passed")
                .setPreviousState("failed")
                .setRepository(webRepository())
                .setCommit(luckyCommit())
                .buildPartial();
    }

    private static Build failedBuild() {
        return Build
                .newBuilder()
                .setId(123152L)
                .setNumber("41")
                .setState("failed")
                .setPreviousState("failed")
                .setRepository(webRepository())
                .setCommit(luckyCommit())
                .buildPartial();
    }

    private static Commit luckyCommit() {
        var compareUrl = "https://github.com/SpineEventEngine/web/compare/6b4d32cadd9c...6b0a31d033a2";
        var author = Commit.Author
                .newBuilder()
                .setName("God")
                .buildPartial();
        return Commit
                .newBuilder()
                .setId(667)
                .setCompareUrl(compareUrl)
                .setSha("6b0a31d033a2fc8d29d49baad600bc31789d9615")
                .setAuthor(author)
                .setCommittedAt("2020-06-06T18:00:00Z")
                .setMessage("No you're not. Fixing the world.")
                .buildPartial();

    }

    private static Commit fatefulCommit() {
        var compareUrl = "https://github.com/SpineEventEngine/web/compare/5cbfa7423708...8fcf5d98e50f";
        var author = Commit.Author
                .newBuilder()
                .setName("Lucifer")
                .buildPartial();
        return Commit
                .newBuilder()
                .setId(666)
                .setCompareUrl(compareUrl)
                .setSha("8fcf5d98e50f8ffa6daa8c81746181c72bd09a50")
                .setAuthor(author)
                .setCommittedAt("2020-06-06T06:06:66Z")
                .setMessage("I am going to conquer the world!")
                .buildPartial();

    }

    private static Repository webRepository() {
        return Repository.newBuilder()
                         .setId(1112)
                         .setName("web")
                         .setSlug(repositoryId.getValue())
                         .buildPartial();
    }
}
