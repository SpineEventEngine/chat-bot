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

import io.spine.base.Time;
import io.spine.chatbot.github.RepositoryId;
import io.spine.chatbot.github.repository.build.BuildState;
import io.spine.chatbot.github.repository.build.BuildStateChange;
import io.spine.chatbot.github.repository.build.RepositoryBuild;
import io.spine.chatbot.github.repository.build.command.CheckRepositoryBuild;
import io.spine.chatbot.github.repository.build.event.BuildStateChanged;
import io.spine.chatbot.travis.Build;
import io.spine.chatbot.travis.Commit;
import io.spine.net.Urls;
import io.spine.server.command.Assign;
import io.spine.server.procman.ProcessManager;

import static io.spine.chatbot.api.TravisClient.defaultTravisClient;
import static io.spine.net.Urls.travisBuildUrlFor;

final class RepositoryBuildProcess
        extends ProcessManager<RepositoryId, RepositoryBuild, RepositoryBuild.Builder> {

    @Assign
    BuildStateChanged handle(CheckRepositoryBuild c) {
        var travis = defaultTravisClient();
        var builds = travis.queryBuildsFor(id().getValue())
                           .getBuildsList();
        if (builds.isEmpty()) {
            throw new RuntimeException("No builds available for repository " + idAsString());
        }
        var build = builds.get(0);
        var buildState = from(build);
        builder().setLastStatusCheck(Time.currentTime())
                 .setBuildState(buildState);
        var stateChange = BuildStateChange
                .newBuilder()
                .setPreviousValue(state().getBuildState())
                .setNewValue(buildState)
                .vBuild();
        return BuildStateChanged
                .newBuilder()
                .setId(c.getId())
                .setChange(stateChange)
                .vBuild();
    }

    private static BuildState from(Build build) {
        var slug = build.getRepository()
                        .getSlug();
        return BuildState
                .newBuilder()
                .setState(build.getState())
                .setPreviousState(build.getPreviousState())
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
}
