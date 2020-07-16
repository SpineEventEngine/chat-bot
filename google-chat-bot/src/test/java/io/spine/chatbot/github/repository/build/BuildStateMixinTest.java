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

package io.spine.chatbot.github.repository.build;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static io.spine.chatbot.github.repository.build.BuildStateMixin.buildStateFrom;
import static io.spine.testing.Tests.nullRef;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

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
}
