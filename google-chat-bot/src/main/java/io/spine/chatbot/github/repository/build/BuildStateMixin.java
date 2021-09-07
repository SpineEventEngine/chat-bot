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

package io.spine.chatbot.github.repository.build;

import io.spine.annotation.GeneratedMixin;

import java.util.EnumSet;

import static io.spine.chatbot.github.repository.build.Build.State.BS_UNKNOWN;
import static io.spine.chatbot.github.repository.build.Build.State.CANCELED;
import static io.spine.chatbot.github.repository.build.Build.State.PASSED;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * Augments {@link Build} with useful methods.
 */
@GeneratedMixin
public interface BuildStateMixin extends BuildOrBuilder {

    /**
     * Determines whether the build is canceled.
     *
     * @return {@code true} if the build is canceled, {@code false} otherwise
     */
    default boolean canceled() {
        return getState() == CANCELED;
    }

    /**
     * Determines whether the build is failed.
     *
     * @return {@code true} if the build is failed, {@code false} otherwise
     * @see #failed(Build.State)
     */
    default boolean failed() {
        return failed(getState());
    }

    /**
     * Returns a capitalized label of the {@linkplain Build.State build state}.
     */
    default String stateLabel() {
        var state = getState();
        var name = state.name();
        var result = name.charAt(0) + name.substring(1)
                                          .toLowerCase();
        return result;
    }

    /**
     * Creates an instance of the {@linkplain Build.State build state} of out its
     * string representation, ignoring the case.
     */
    static Build.State buildStateFrom(String state) {
        checkNotEmptyOrBlank(state);
        return Build.State.valueOf(state.toUpperCase());
    }

    /**
     * Determines the {@linkplain BuildStateChange state change} of the build comparing to the
     * {@code previousState}.
     *
     * @see #stateChange(BuildStateMixin, BuildStateMixin)
     */
    default BuildStateChange.Type stateChangeFrom(BuildStateMixin previousState) {
        return stateChange(this, previousState);
    }

    /**
     * Determines the {@linkplain BuildStateChange state change} between build states.
     *
     * <p>The status is considered:
     *
     * <ul>
     *     <li>{@code failed} if the new state is {@link #failed() failed};
     *     <li>{@code canceled} if the new state is {@link #canceled() canceled} and
     *     the previous is non-canceled. This way only notify about cancellation once;
     *     <li>{@code recovered} if the new state is {@code passed} and the previous is
     *     {@link #failed() failed};
     *     <li>{@code stable} if the new state is {@code passed} or {@code canceled} and
     *     the previous is either {@code unknown} meaning that there were no previous states
     *     or {@code passed} or {@code canceled} as well.
     * </ul>
     */
    private static BuildStateChange.Type stateChange(BuildStateMixin newBuildState,
                                                     BuildStateMixin previousBuildState) {
        var currentState = newBuildState.getState();
        var previousState = previousBuildState.getState();
        if (newBuildState.failed()) {
            return BuildStateChange.Type.FAILED;
        }
        if (newBuildState.canceled() && !previousBuildState.canceled()) {
            return BuildStateChange.Type.CANCELED;
        }
        if (currentState == PASSED && previousBuildState.failed()) {
            return BuildStateChange.Type.RECOVERED;
        }
        if (stable(currentState, previousState)) {
            return BuildStateChange.Type.STABLE;
        }
        throw newIllegalStateException(
                "Build is in an unpredictable state. Current state `%s`. Previous state `%s`.",
                currentState.name(), previousState.name()
        );
    }

    private static boolean stable(Build.State currentState, Build.State previousState) {
        var stableStates = EnumSet.of(PASSED, BS_UNKNOWN, CANCELED);
        return stableStates.contains(currentState) && stableStates.contains(previousState);
    }

    /**
     * Determines whether the build state denotes a failed status.
     *
     * <p>The {@code failed} and {@code errored} statuses are considered failed statuses.
     *
     * @return {@code true} if the build status is failed, {@code false} otherwise
     */
    private static boolean failed(Build.State state) {
        var failedStatuses = EnumSet.of(Build.State.FAILED, Build.State.ERRORED);
        return failedStatuses.contains(state);
    }
}
