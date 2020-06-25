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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A utility for working with {@link GitHubContext} identifiers.
 */
public final class GitHubIdentifier {

    /**
     * Prevents instantiation of this utility class.
     */
    private GitHubIdentifier() {
    }

    /**
     * Creates a new {@link OrganizationId} out of the specified {@code name}.
     */
    public static OrganizationId organization(String name) {
        checkNotNull(name);
        return OrganizationId
                .newBuilder()
                .setValue(name)
                .vBuild();
    }

    /**
     * Creates a new {@link RepositoryId} out of the specified {@code slug}.
     */
    public static RepositoryId repository(String slug) {
        checkNotNull(slug);
        return RepositoryId
                .newBuilder()
                .setValue(slug)
                .vBuild();
    }
}
