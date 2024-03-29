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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.chatbot.github.repository.build.BuildStateMixin.buildStateFrom;
import static io.spine.testing.TestValues.nullRef;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;

@DisplayName("`BuildStateMixin` should")
final class BuildStateMixinTest {

    @Test
    @DisplayName("not accept `null` build states")
    void notAcceptNull() {
        assertThrows(NullPointerException.class, () -> buildStateFrom(nullRef()));
    }

    @Test
    @DisplayName("not accept unknown build states")
    void notAcceptUnknownStates() {
        assertThrows(IllegalArgumentException.class, () -> buildStateFrom("unknown"));
    }

    @DisplayName("accept valid `Build.State` value")
    @ParameterizedTest
    @EnumSource(mode = EXCLUDE, value = Build.State.class, names = {"BS_UNKNOWN", "UNRECOGNIZED"})
    void processValidValues(Build.State state) {
        assertDoesNotThrow(() -> {
            buildStateFrom(state.name());
        });
    }

    @DisplayName("determine failed states")
    @ParameterizedTest
    @EnumSource(mode = INCLUDE, value = Build.State.class, names = {"FAILED", "ERRORED"})
    void determineFailed(Build.State state) {
        Build build = Build.newBuilder()
                .setState(state)
                .buildPartial();
        assertThat(build.failed())
                .isTrue();
    }

    @DisplayName("determine states as non-failed")
    @ParameterizedTest
    @EnumSource(mode = EXCLUDE, value = Build.State.class, names = {"FAILED", "ERRORED", "UNRECOGNIZED"})
    void determineNonFailed(Build.State state) {
        Build build = Build.newBuilder()
                .setState(state)
                .buildPartial();
        assertThat(build.failed())
                .isFalse();
    }

    @Nested
    @DisplayName("determine build state change")
    class StateChange {

        @Test
        @DisplayName("as failed no matter what")
        void failed() {
            var previous = Build.newBuilder()
                    .setState(Build.State.PASSED)
                    .buildPartial();
            var current = Build.newBuilder()
                    .setState(Build.State.FAILED)
                    .buildPartial();
            assertThat(current.stateChangeFrom(previous))
                    .isEqualTo(BuildStateChange.Type.FAILED);
        }

        @Test
        @DisplayName("as canceled no matter what")
        void canceled() {
            var previous = Build.newBuilder()
                    .setState(Build.State.FAILED)
                    .buildPartial();
            var current = Build.newBuilder()
                    .setState(Build.State.CANCELED)
                    .buildPartial();
            assertThat(current.stateChangeFrom(previous))
                    .isEqualTo(BuildStateChange.Type.CANCELED);
        }

        @Test
        @DisplayName("as recovered if the previous was failed")
        void recovered() {
            var previous = Build.newBuilder()
                    .setState(Build.State.FAILED)
                    .buildPartial();
            var current = Build.newBuilder()
                    .setState(Build.State.PASSED)
                    .buildPartial();
            assertThat(current.stateChangeFrom(previous))
                    .isEqualTo(BuildStateChange.Type.RECOVERED);
        }

        @DisplayName("as stable if the previous was")
        @ParameterizedTest
        @EnumSource(mode = INCLUDE, value = Build.State.class, names = {"PASSED", "BS_UNKNOWN", "CANCELED"})
        void stable(Build.State state) {
            var previous = Build.newBuilder()
                    .setState(state)
                    .buildPartial();
            var current = Build.newBuilder()
                    .setState(state)
                    .buildPartial();
            assertThat(current.stateChangeFrom(previous))
                    .isEqualTo(BuildStateChange.Type.STABLE);
        }
    }
}
