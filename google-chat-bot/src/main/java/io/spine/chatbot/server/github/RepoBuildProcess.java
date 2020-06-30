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
import com.google.errorprone.annotations.concurrent.LazyInit;
import io.spine.base.Time;
import io.spine.chatbot.api.travis.BuildsQuery;
import io.spine.chatbot.api.travis.Commit;
import io.spine.chatbot.api.travis.RepoBranchBuildResponse;
import io.spine.chatbot.api.travis.TravisClient;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.repository.build.BuildState;
import io.spine.chatbot.github.repository.build.BuildStateChange;
import io.spine.chatbot.github.repository.build.RepositoryBuild;
import io.spine.chatbot.github.repository.build.command.CheckRepositoryBuild;
import io.spine.chatbot.github.repository.build.event.BuildFailed;
import io.spine.chatbot.github.repository.build.event.BuildRecovered;
import io.spine.chatbot.github.repository.build.event.BuildSucceededAgain;
import io.spine.chatbot.github.repository.build.rejection.NoBuildsFound;
import io.spine.logging.Logging;
import io.spine.net.Urls;
import io.spine.server.command.Assign;
import io.spine.server.procman.ProcessManager;
import io.spine.server.tuple.EitherOf3;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static io.spine.net.Urls.travisBuildUrlFor;
import static io.spine.protobuf.Messages.isDefault;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Verifies a status of a build of a repository.
 *
 * <p>Performs repository build checks and acknowledges the state of the repository builds.
 * As a result, emits build status events such as:
 *
 * <ul>
 *     <li>{@link BuildFailed} — whenever the build is failed;
 *     <li>{@link BuildRecovered} — whenever the build state changes from {@code failed}
 *     to {@code passing};
 *     <li>{@link BuildSucceededAgain} — whenever the build state is {@code passing} and was
 *     {@code passing} previously.
 * </ul>
 *
 * Or, if the repository builds could not be retrieved, throws {@link NoBuildsFound} rejection.
 */
final class RepoBuildProcess
        extends ProcessManager<RepositoryId, RepositoryBuild, RepositoryBuild.Builder>
        implements Logging {

    @LazyInit
    private @MonotonicNonNull TravisClient client;

    /**
     * Checks the repository build state and propagates the respective events.
     *
     * <p>If the repository build state could not be retrieved,
     * throws {@link NoBuildsFound} rejection.
     */
    @Assign
    EitherOf3<BuildFailed, BuildRecovered, BuildSucceededAgain> handle(CheckRepositoryBuild c)
            throws NoBuildsFound {
        var repository = c.getRepository();
        _info().log("Checking build status for the repository `%s`.", repository.getValue());
        var branchBuild = client.execute(BuildsQuery.forRepo(repository.getValue()));
        if (isDefault(branchBuild.getLastBuild())) {
            _warn().log("No builds found for the repository `%s`.", repository.getValue());
            throw NoBuildsFound
                    .newBuilder()
                    .setRepository(repository)
                    .build();
        }
        var buildState = buildStateFrom(branchBuild, c.getGoogleChatSpace());
        builder().setLastStatusCheck(Time.currentTime())
                 .setRepositoryBuildState(buildState.getState())
                 .setBuildState(buildState);
        var stateChange = BuildStateChange
                .newBuilder()
                .setPreviousValue(state().getBuildState())
                .setNewValue(buildState)
                .vBuild();
        var result = determineOutcome(repository, stateChange);
        return result;
    }

    private EitherOf3<BuildFailed, BuildRecovered, BuildSucceededAgain>
    determineOutcome(RepositoryId repository, BuildStateChange stateChange) {
        var newBuildState = stateChange.getNewValue();
        var previousBuildState = stateChange.getPreviousValue();
        var stateStatusChange = newBuildState.stateChangeFrom(previousBuildState);
        switch (stateStatusChange) {
            case FAILED:
                return onFailed(repository, stateChange);
            case RECOVERED:
                return onRecovered(repository, stateChange);
            case STABLE:
                return onStable(repository, stateChange);
            case BSC_UNKNOWN:
            case UNRECOGNIZED:
            default:
                throw newIllegalStateException(
                        "Unexpected state status change `%s`.", stateStatusChange
                );
        }
    }

    private EitherOf3<BuildFailed, BuildRecovered, BuildSucceededAgain>
    onStable(RepositoryId repository, BuildStateChange stateChange) {
        _info().log("Build for the repository `%s` is stable.", repository.getValue());
        var buildSucceededAgain = BuildSucceededAgain
                .newBuilder()
                .setRepository(repository)
                .setChange(stateChange)
                .vBuild();
        return EitherOf3.withC(buildSucceededAgain);
    }

    private EitherOf3<BuildFailed, BuildRecovered, BuildSucceededAgain>
    onRecovered(RepositoryId repository, BuildStateChange stateChange) {
        _info().log("Build for the repository `%s` is recovered.", repository.getValue());
        var buildRecovered = BuildRecovered
                .newBuilder()
                .setRepository(repository)
                .setChange(stateChange)
                .vBuild();
        return EitherOf3.withB(buildRecovered);
    }

    private EitherOf3<BuildFailed, BuildRecovered, BuildSucceededAgain>
    onFailed(RepositoryId repository, BuildStateChange stateChange) {
        var newBuildState = stateChange.getNewValue();
        _info().log("Build for the repository `%s` failed with status `%s`.",
                    repository.getValue(), newBuildState.getState());
        var buildFailed = BuildFailed
                .newBuilder()
                .setRepository(repository)
                .setChange(stateChange)
                .vBuild();
        return EitherOf3.withA(buildFailed);
    }

    @VisibleForTesting
    static BuildState buildStateFrom(RepoBranchBuildResponse branchBuild, String space) {
        var branchBuildName = branchBuild.getName();
        var slug = branchBuild.getRepository()
                              .getSlug();
        var build = branchBuild.getLastBuild();
        return BuildState
                .newBuilder()
                .setNumber(build.getNumber())
                .setGoogleChatSpace(space)
                .setState(BuildStateMixin.buildStateFrom(build.getState()))
                .setPreviousState(BuildStateMixin.buildStateFrom(build.getPreviousState()))
                .setBranch(branchBuildName)
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

    /**
     * Sets {@link #client} to be used during handling of signals.
     *
     * @implNote the method is intended to be used as part of the entity configuration
     *         done through the repository
     */
    void setClient(TravisClient client) {
        this.client = client;
    }
}
