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

package io.spine.chatbot.github.repository;

import io.spine.annotation.GeneratedMixin;
import io.spine.chatbot.github.OrganizationId;
import io.spine.net.Url;

/**
 * Common interface for messages aware of the {@link RepoHeader}.
 */
@GeneratedMixin
public interface RepoHeaderAware {

    /**
     * Returns the repository {@code header}.
     *
     * @implNote This method is implemented in the deriving Protobuf messages.
     */
    RepoHeader getHeader();

    /**
     * Returns the repository {@code name}.
     */
    default String name() {
        return getHeader().getName();
    }

    /**
     * Returns the repository {@code githubProfile}.
     */
    default Url githubProfile() {
        return getHeader().getGithubProfile();
    }

    /**
     * Returns the repository {@code travisProfile}.
     */
    default Url travisProfile() {
        return getHeader().getTravisProfile();
    }

    /**
     * Returns the organization associated with the repository.
     */
    default OrganizationId organization() {
        return getHeader().getOrganization();
    }
}
