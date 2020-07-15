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

import io.spine.chatbot.github.OrganizationId;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.Slugs;
import io.spine.chatbot.github.repository.build.Build;
import io.spine.chatbot.github.repository.build.BuildStateChange;
import io.spine.chatbot.github.repository.build.RepositoryBuild;
import io.spine.chatbot.github.repository.build.command.CheckRepositoryBuild;
import io.spine.chatbot.github.repository.build.event.BuildFailed;
import io.spine.chatbot.github.repository.build.event.BuildRecovered;
import io.spine.chatbot.github.repository.build.event.BuildSucceededAgain;
import io.spine.chatbot.github.repository.build.rejection.RepositoryBuildRejections;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.travis.Author;
import io.spine.chatbot.travis.Commit;
import io.spine.chatbot.travis.RepoBranchBuildResponse;
import io.spine.chatbot.travis.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.chatbot.server.github.GitHubIdentifiers.organization;
import static io.spine.chatbot.server.github.GitHubIdentifiers.repository;
import static io.spine.chatbot.server.github.RepoBuildProcess.buildFrom;
import static io.spine.chatbot.server.google.chat.GoogleChatIdentifiers.space;

@DisplayName("`RepoBuildProcess` should")
final class RepoBuildProcessTest extends GitHubContextAwareTest {

    private static final OrganizationId org = organization("SpineEventEngine");
    private static final RepositoryId repo = repository("SpineEventEngine/web");
    private static final SpaceId space = space("spaces/1245wrq");

    @Test
    @DisplayName("throw `NoBuildsFound` rejection when Travis API cannot return builds for a repo")
    void throwNoBuildsFoundRejection() {
        travisClient().setBuildsFor(Slugs.forRepo(repo),
                                    RepoBranchBuildResponse.getDefaultInstance());
        var checkRepoBuild = CheckRepositoryBuild
                .newBuilder()
                .setRepository(repo)
                .setOrganization(org)
                .setSpace(space)
                .vBuild();
        context().receivesCommand(checkRepoBuild);

        var noBuildsFound = RepositoryBuildRejections.NoBuildsFound
                .newBuilder()
                .setRepository(repo)
                .vBuild();
        context().assertEvent(noBuildsFound);
    }

    @SuppressWarnings("ClassCanBeStatic") // nested tests do not work with static classes
    @Nested
    @DisplayName("handle build failure")
    final class FailedBuild {

        private final io.spine.chatbot.travis.Build build = failedBuild();
        private final RepoBranchBuildResponse branchBuild = branchBuildOf(build);
        private final Build buildState = buildFrom(branchBuild, space);

        @BeforeEach
        void sendCheckCommand() {
            travisClient().setBuildsFor(Slugs.forRepo(repo), branchBuild);
            var checkRepoBuild = CheckRepositoryBuild
                    .newBuilder()
                    .setRepository(repo)
                    .setSpace(space)
                    .setOrganization(org)
                    .vBuild();
            context().receivesCommand(checkRepoBuild);
        }

        @Test
        @DisplayName("producing `BuildFailed` event")
        void producingEvent() {
            var stateChange = BuildStateChange
                    .newBuilder()
                    .setNewValue(buildState)
                    .vBuild();
            var buildFailed = BuildFailed
                    .newBuilder()
                    .setRepository(repo)
                    .setChange(stateChange)
                    .vBuild();
            context().assertEvent(buildFailed);
        }

        @Test
        @DisplayName("setting process state")
        void settingState() {
            var expectedState = RepositoryBuild
                    .newBuilder()
                    .setRepository(repo)
                    .setBuild(buildState)
                    .setCurrentState(buildState.getState())
                    .vBuild();
            context().assertState(repo, RepositoryBuild.class)
                     .isEqualTo(expectedState);
        }
    }

    @SuppressWarnings("ClassCanBeStatic") // nested tests do not work with static classes
    @Nested
    @DisplayName("handle build recovery")
    final class RecoveredBuild {

        private final io.spine.chatbot.travis.Build previousBuild = failedBuild();
        private final RepoBranchBuildResponse previousBranchBuild = branchBuildOf(previousBuild);
        private final Build previousBuildState = buildFrom(previousBranchBuild,
                                                           space);

        private final io.spine.chatbot.travis.Build newBuild = passingBuild();
        private final RepoBranchBuildResponse newBranchBuild = branchBuildOf(newBuild);
        private final Build newBuildState = buildFrom(newBranchBuild, space);

        @BeforeEach
        void sendCheckCommands() {
            travisClient().setBuildsFor(Slugs.forRepo(repo), previousBranchBuild);
            var checkRepoFailure = CheckRepositoryBuild
                    .newBuilder()
                    .setRepository(repo)
                    .setSpace(space)
                    .setOrganization(org)
                    .vBuild();
            context().receivesCommand(checkRepoFailure);
            travisClient().setBuildsFor(Slugs.forRepo(repo), newBranchBuild);
            var checkRepoRecovery = CheckRepositoryBuild
                    .newBuilder()
                    .setRepository(repo)
                    .setSpace(space)
                    .setOrganization(org)
                    .vBuild();
            context().receivesCommand(checkRepoRecovery);
        }

        @Test
        @DisplayName("producing `BuildRecovered` event")
        void producingEvent() {
            var stateChange = BuildStateChange
                    .newBuilder()
                    .setPreviousValue(previousBuildState)
                    .setNewValue(newBuildState)
                    .vBuild();
            var buildFailed = BuildRecovered
                    .newBuilder()
                    .setRepository(repo)
                    .setChange(stateChange)
                    .vBuild();
            context().assertEvent(buildFailed);
        }

        @Test
        @DisplayName("setting process state")
        void settingState() {
            var expectedState = RepositoryBuild
                    .newBuilder()
                    .setRepository(repo)
                    .setBuild(newBuildState)
                    .setCurrentState(newBuildState.getState())
                    .vBuild();
            context().assertState(repo, RepositoryBuild.class)
                     .isEqualTo(expectedState);
        }
    }

    @SuppressWarnings("ClassCanBeStatic") // nested tests do not work with static classes
    @Nested
    @DisplayName("handle stable builds")
    final class StableBuild {

        private final io.spine.chatbot.travis.Build initialFailedBuild = failedBuild();

        private final io.spine.chatbot.travis.Build previousBuild = passingBuild();
        private final RepoBranchBuildResponse previousBranchBuild = branchBuildOf(previousBuild);
        private final Build previousBuildState = buildFrom(previousBranchBuild,
                                                           space);

        private final io.spine.chatbot.travis.Build newBuild = nextPassingBuild();
        private final RepoBranchBuildResponse newBranchBuild = branchBuildOf(newBuild);
        private final Build newBuildState = buildFrom(newBranchBuild, space);

        @BeforeEach
        void sendCheckCommands() {
            travisClient().setBuildsFor(Slugs.forRepo(repo),
                                        branchBuildOf(initialFailedBuild));
            var checkRepoFailure = CheckRepositoryBuild
                    .newBuilder()
                    .setRepository(repo)
                    .setSpace(space)
                    .setOrganization(org)
                    .vBuild();
            context().receivesCommand(checkRepoFailure);
            travisClient().setBuildsFor(Slugs.forRepo(repo), previousBranchBuild);
            var checkRepoRecovery = CheckRepositoryBuild
                    .newBuilder()
                    .setRepository(repo)
                    .setSpace(space)
                    .setOrganization(org)
                    .vBuild();
            context().receivesCommand(checkRepoRecovery);
            travisClient().setBuildsFor(Slugs.forRepo(repo), newBranchBuild);
            var checkRepoStable = CheckRepositoryBuild
                    .newBuilder()
                    .setRepository(repo)
                    .setSpace(space)
                    .setOrganization(org)
                    .vBuild();
            context().receivesCommand(checkRepoStable);
        }

        @Test
        @DisplayName("producing `BuildSucceededAgain` event")
        void producingEvent() {
            var stateChange = BuildStateChange
                    .newBuilder()
                    .setPreviousValue(previousBuildState)
                    .setNewValue(newBuildState)
                    .vBuild();
            var buildSucceededAgain = BuildSucceededAgain
                    .newBuilder()
                    .setRepository(repo)
                    .setChange(stateChange)
                    .vBuild();
            context().assertEvent(buildSucceededAgain);
        }

        @Test
        @DisplayName("setting process state")
        void settingState() {
            var expectedState = RepositoryBuild
                    .newBuilder()
                    .setRepository(repo)
                    .setBuild(newBuildState)
                    .setCurrentState(newBuildState.getState())
                    .vBuild();
            context().assertState(repo, RepositoryBuild.class)
                     .isEqualTo(expectedState);
        }
    }

    private static RepoBranchBuildResponse branchBuildOf(io.spine.chatbot.travis.Build build) {
        return RepoBranchBuildResponse
                .newBuilder()
                .setLastBuild(build)
                .setName("master")
                .setRepository(Repository.newBuilder()
                                         .setSlug(repo.getValue()))
                .buildPartial();
    }

    private static io.spine.chatbot.travis.Build passingBuild() {
        return io.spine.chatbot.travis.Build
                .newBuilder()
                .setId(123153L)
                .setNumber("42")
                .setState("passed")
                .setPreviousState("failed")
                .setRepository(webRepository())
                .setCommit(luckyCommit())
                .buildPartial();
    }

    private static io.spine.chatbot.travis.Build nextPassingBuild() {
        return io.spine.chatbot.travis.Build
                .newBuilder()
                .setId(123154L)
                .setNumber("43")
                .setState("passed")
                .setPreviousState("passed")
                .setRepository(webRepository())
                .setCommit(stableCommit())
                .buildPartial();
    }

    private static io.spine.chatbot.travis.Build failedBuild() {
        return io.spine.chatbot.travis.Build
                .newBuilder()
                .setId(123152L)
                .setNumber("41")
                .setState("failed")
                .setPreviousState("failed")
                .setRepository(webRepository())
                .setCommit(fatefulCommit())
                .buildPartial();
    }

    private static Commit stableCommit() {
        var compareUrl = "https://github.com/SpineEventEngine/web/compare/04694f26f24a...afc1b76bf93c";
        var author = Author
                .newBuilder()
                .setName("God")
                .buildPartial();
        return Commit
                .newBuilder()
                .setId(668)
                .setCompareUrl(compareUrl)
                .setSha("afc1b76bf93c4dadf86075280da623f947e1434b")
                .setAuthor(author)
                .setCommittedAt("2020-06-06T18:30:00Z")
                .setMessage("May the heaven be on earth.")
                .buildPartial();
    }

    private static Commit luckyCommit() {
        var compareUrl = "https://github.com/SpineEventEngine/web/compare/6b4d32cadd9c...6b0a31d033a2";
        var author = Author
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
        var author = Author
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
        return Repository
                .newBuilder()
                .setId(1112)
                .setName("web")
                .setSlug(repo.getValue())
                .buildPartial();
    }
}
