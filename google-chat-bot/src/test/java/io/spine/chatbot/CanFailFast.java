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

package io.spine.chatbot;

import io.spine.logging.Logging;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * An abstract API that exposes the {@code fail-fast} concept.
 */
public abstract class CanFailFast implements Logging {

    /**
     * Determines whether the client should fail if a particular response is not preconfigured.
     */
    private final boolean failFast;

    /**
     * Creates a new client with the specified {@code failFast} behavior.
     */
    protected CanFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    /**
     * Applies the fail-fast approach to the supplied value if the client is configured so,
     * otherwise returns the {@code defaultValue} if the supplied {@code value} is {@code null}.
     *
     * @param value
     *         the value under check
     * @param key
     *         the key using which the {@code value} was obtained
     * @param defaultValue
     *         the default value to be used if the client is not using the fail-fast approach and
     *         the value is {@code null}
     * @param <K>
     *         the type of the key the API client was called with
     * @param <V>
     *         the type of the value the API client is expected to return
     * @throws IllegalStateException
     *         if the client is configured to use the fail-fast approach and the supplied
     *         {@code value} is {@code null}
     */
    protected <K, V> @NonNull V failOrDefault(@Nullable V value,
                                              @NonNull K key,
                                              @NonNull V defaultValue) {
        if (failFast && value == null) {
            throw newIllegalStateException(
                    "Response of type `%s` is not configured for the key `%s`.",
                    defaultValue.getClass()
                                .getSimpleName(), String.valueOf(key)
            );
        }
        if (!failFast && value == null) {
            return defaultValue;
        }
        return value;
    }
}
