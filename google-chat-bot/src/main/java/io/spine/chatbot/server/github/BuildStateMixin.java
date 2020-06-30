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

import io.spine.annotation.GeneratedMixin;
import io.spine.chatbot.github.repository.build.Build;
import io.spine.chatbot.github.repository.build.BuildOrBuilder;
import io.spine.chatbot.github.repository.build.BuildStateChange;

import java.util.EnumSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.chatbot.github.repository.build.Build.State.BS_UNKNOWN;
import static io.spine.chatbot.github.repository.build.Build.State.PASSED;
import static io.spine.chatbot.github.repository.build.BuildStateChange.Type.FAILED;
import static io.spine.chatbot.github.repository.build.BuildStateChange.Type.RECOVERED;
import static io.spine.chatbot.github.repository.build.BuildStateChange.Type.STABLE;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Augments {@link Build} with useful methods.
 */
@GeneratedMixin
public interface BuildStateMixin extends BuildOrBuilder {

    /**
     * Determines whether the build is failed.
     *
     * @return `true` if the build is failed, `false` otherwise
     * @see #failed(Build.State)
     */
    default boolean failed() {
        return failed(getState());
    }

    /**
     * Returns a capitalized label of the {@link Build.State build state}.
     */
    default String stateLabel() {
        var state = getState();
        var name = state.name();
        var result = name.charAt(0) + name.substring(1)
                                          .toLowerCase();
        return result;
    }

    /**
     * Creates an instance of the {@link Build.State build state} of out its
     * string representation.
     */
    static Build.State buildStateFrom(String state) {
        checkNotNull(state);
        for (Build.State buildState : Build.State.values()) {
            if (state.equalsIgnoreCase(buildState.name())) {
                return buildState;
            }
        }
        throw newIllegalArgumentException(
                "Unable to create build state out of the supplied string value `%s`.", state
        );
    }

    /**
     * Determines the {@link BuildStateChange state chage} of the build comparing to the
     * {@code previousState}.
     *
     * @see #stateChange(BuildStateMixin, BuildStateMixin)
     */
    default BuildStateChange.Type stateChangeFrom(BuildStateMixin previousState) {
        return stateChange(this, previousState);
    }

    /**
     * Determines the {@link BuildStateChange state chage} between build states.
     *
     * <p>The status is considered:
     *
     * <ul>
     *     <li>{@code failed} if the new state is {@link #failed() failed}.
     *     <li>{@code recovered} if the new state is {@code passed} and the previous is
     *     {@link #failed() failed}.
     *     <li>{@code stable} if the new state is {@code passed} and the previous is either
     *     {@code unknown} meaning that there were no previous states or {@code passed} as well.
     * </ul>
     */
    private static BuildStateChange.Type stateChange(BuildStateMixin newBuildState,
                                                     BuildStateMixin previousBuildState) {
        var currentState = newBuildState.getState();
        var previousState = previousBuildState.getState();
        if (newBuildState.failed()) {
            return FAILED;
        }
        if (currentState == PASSED && previousBuildState.failed()) {
            return RECOVERED;
        }
        if (currentState == PASSED && (previousState == PASSED || previousState == BS_UNKNOWN)) {
            return STABLE;
        }
        throw newIllegalStateException(
                "Build is in an unpredictable state. Current state `%s`. Previous state `%s`.",
                currentState.name(), previousState.name()
        );
    }

    /**
     * Determines whether the build state denotes a failed status.
     *
     * <p>The {@code cancelled}, {@code failed} and {@code errored} statuses are considered
     * failed statuses.
     *
     * @return {@code true} if the build status is failed, {@code false} otherwise
     */
    private static boolean failed(Build.State state) {
        var failedStatuses = EnumSet.of(
                Build.State.CANCELLED, Build.State.FAILED, Build.State.ERRORED
        );
        return failedStatuses.contains(state);
    }
}
