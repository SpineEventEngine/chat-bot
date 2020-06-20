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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.concurrent.LazyInit;
import io.spine.base.Time;
import io.spine.chatbot.api.travis.Build;
import io.spine.chatbot.api.travis.BuildsQuery;
import io.spine.chatbot.api.travis.Commit;
import io.spine.chatbot.api.travis.TravisClient;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.repository.build.BuildState;
import io.spine.chatbot.github.repository.build.BuildStateChange;
import io.spine.chatbot.github.repository.build.BuildStateStatusChange;
import io.spine.chatbot.github.repository.build.RepositoryBuild;
import io.spine.chatbot.github.repository.build.command.CheckRepositoryBuild;
import io.spine.chatbot.github.repository.build.event.BuildFailed;
import io.spine.chatbot.github.repository.build.event.BuildRecovered;
import io.spine.chatbot.github.repository.build.event.BuildStable;
import io.spine.chatbot.github.repository.build.rejection.NoBuildsFound;
import io.spine.net.Urls;
import io.spine.server.command.Assign;
import io.spine.server.procman.ProcessManager;
import io.spine.server.tuple.EitherOf3;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static io.spine.chatbot.github.repository.build.BuildState.State.PASSED;
import static io.spine.chatbot.github.repository.build.BuildStateStatusChange.FAILED;
import static io.spine.chatbot.github.repository.build.BuildStateStatusChange.RECOVERED;
import static io.spine.chatbot.github.repository.build.BuildStateStatusChange.STABLE;
import static io.spine.net.Urls.travisBuildUrlFor;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A repository build process.
 *
 * <p>Performs repository build checks and acknowledges the state of the repository builds.
 * As a result, emits build status events such as:
 *
 * <ul>
 *     <li>{@link BuildFailed} — whenever the build is failed;
 *     <li>{@link BuildRecovered} — whenever the build state changes from {@code failed}
 *     to {@code passing}.
 *     <li>{@link BuildStable} — whenever the build state is {@code passing} and was
 *     {@code passing} previously.
 * </ul>
 *
 * Or, if the repository builds could not be retrieved, throws {@link NoBuildsFound} rejection.
 *
 * <p>The {@code cancelled}, {@code failed} and {@code errored} statuses are considered
 * {@link #FAILED_STATUSES failed statuses}.
 */
final class RepoBuildProcess
        extends ProcessManager<RepositoryId, RepositoryBuild, RepositoryBuild.Builder> {

    private static final ImmutableSet<BuildState.State> FAILED_STATUSES = ImmutableSet.of(
            BuildState.State.CANCELLED, BuildState.State.FAILED, BuildState.State.ERRORED
    );

    @LazyInit
    private @MonotonicNonNull TravisClient travisClient;

    /**
     * Checks the repository build state and propagates the respective events.
     *
     * <p>If the repository build state could not be retrieved,
     * throws {@link NoBuildsFound} rejection.
     */
    @Assign
    EitherOf3<BuildFailed, BuildRecovered, BuildStable> handle(CheckRepositoryBuild c)
            throws NoBuildsFound {
        var builds = travisClient.execute(BuildsQuery.forRepo(id().getValue()))
                                 .getBuildsList();
        if (builds.isEmpty()) {
            throw NoBuildsFound
                    .newBuilder()
                    .setId(c.getId())
                    .build();
        }
        var build = builds.get(0);
        var buildState = buildStateFrom(build);
        builder().setLastStatusCheck(Time.currentTime())
                 .setBuildState(buildState);
        var stateChange = BuildStateChange
                .newBuilder()
                .setPreviousValue(state().getBuildState())
                .setNewValue(buildState)
                .vBuild();
        var result = determineOutcome(c.getId(), stateChange);
        return result;
    }

    private static EitherOf3<BuildFailed, BuildRecovered, BuildStable>
    determineOutcome(RepositoryId id, BuildStateChange stateChange) {
        var stateStatusChange = stateStatusChangeOf(stateChange.getNewValue());
        switch (stateStatusChange) {
            case FAILED:
                var buildFailed = BuildFailed
                        .newBuilder()
                        .setId(id)
                        .setChange(stateChange)
                        .vBuild();
                return EitherOf3.withA(buildFailed);
            case RECOVERED:
                var buildRecovered = BuildRecovered
                        .newBuilder()
                        .setId(id)
                        .setChange(stateChange)
                        .vBuild();
                return EitherOf3.withB(buildRecovered);
            case STABLE:
                var buildStable = BuildStable
                        .newBuilder()
                        .setId(id)
                        .setChange(stateChange)
                        .vBuild();
                return EitherOf3.withC(buildStable);
            case BSC_UNKNOWN:
            case UNRECOGNIZED:
            default:
                throw newIllegalStateException(
                        "Unexpected state status change `%s`.", stateStatusChange
                );
        }
    }

    private static BuildStateStatusChange stateStatusChangeOf(BuildState buildState) {
        var currentState = buildState.getState();
        var previousState = buildState.getPreviousState();
        if (FAILED_STATUSES.contains(currentState)) {
            return FAILED;
        }
        if (currentState == PASSED && FAILED_STATUSES.contains(previousState)) {
            return RECOVERED;
        }
        if (currentState == PASSED && previousState == PASSED) {
            return STABLE;
        }
        throw newIllegalStateException(
                "Build is in an unpredictable state. Current state `%s`. Previous state `%s`.",
                currentState.name(), previousState.name()
        );
    }

    @VisibleForTesting
    static BuildState buildStateFrom(Build build) {
        var slug = build.getRepository()
                        .getSlug();
        return BuildState
                .newBuilder()
                .setState(BuildStates.buildStateFrom(build.getState()))
                .setPreviousState(BuildStates.buildStateFrom(build.getPreviousState()))
                .setBranch(build.getBranch()
                                .getName())
                .setLastCommit(from(build.getCommit()))
                .setCreatedBy(build.getCreatedBy()
                                   .getLogin())
                .setRepositorySlug(slug)
                .setTravisCiUrl(travisBuildUrlFor(slug, build.getId()))
                .vBuild();
    }

    private static BuildState.Commit from(Commit commit) {
        return BuildState.Commit
                .newBuilder()
                .setSha(commit.getSha())
                .setMessage(commit.getMessage())
                .setCommittedAt(commit.getCommittedAt())
                .setAuthoredBy(commit.getAuthor()
                                     .getName())
                .setCompareUrl(Urls.urlOfSpec(commit.getCompareUrl()))
                .vBuild();
    }

    void setTravisClient(TravisClient travisClient) {
        this.travisClient = travisClient;
    }
}
