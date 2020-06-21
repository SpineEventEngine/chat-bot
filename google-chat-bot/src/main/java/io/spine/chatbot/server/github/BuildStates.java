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

import com.google.common.collect.ImmutableSet;
import io.spine.chatbot.github.repository.build.BuildState;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalArgumentException;

/**
 * A utility class for working with {@link BuildState.State}s.
 */
public final class BuildStates {

    /** Failed Travis build states. **/
    private static final ImmutableSet<BuildState.State> FAILED_STATUSES = ImmutableSet.of(
            BuildState.State.CANCELLED, BuildState.State.FAILED, BuildState.State.ERRORED
    );

    /**
     * Prevents instantiation of this utility class.
     */
    private BuildStates() {
    }

    /**
     * Creates an instance of the build state of out its string representation.
     */
    static BuildState.State buildStateFrom(String state) {
        checkNotNull(state);
        for (BuildState.State buildState : BuildState.State.values()) {
            if (state.equalsIgnoreCase(buildState.name())) {
                return buildState;
            }
        }
        throw newIllegalArgumentException(
                "Unable to create build state out of the supplied string value `%s`.", state
        );
    }

    /**
     * Determines whether the build is failed.
     *
     * @return `true` if the build is failed, `false` otherwise
     * @see #isFailed(BuildState.State)
     */
    public static boolean isFailed(BuildState buildState) {
        checkNotNull(buildState);
        return isFailed(buildState.getState());
    }

    /**
     * Determines whether the build state denotes a filed status.
     *
     * <p>The {@code cancelled}, {@code failed} and {@code errored} statuses are considered
     * {@link #FAILED_STATUSES failed statuses}.
     *
     * @return `true` if the build status is failed, `false` otherwise
     */
    public static boolean isFailed(BuildState.State state) {
        checkNotNull(state);
        return FAILED_STATUSES.contains(state);
    }
}
