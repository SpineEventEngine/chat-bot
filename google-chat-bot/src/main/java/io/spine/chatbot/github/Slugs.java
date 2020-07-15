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

package io.spine.chatbot.github;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A utility for working with {@link Slug}s.
 */
public final class Slugs {

    /**
     * Prevents instantiation of this utility class.
     */
    private Slugs() {
    }

    /**
     * Creates a new {@code Slug} for the {@code repository}.
     */
    public static Slug forRepo(RepositoryId repo) {
        checkNotNull(repo);
        return create(repo.getValue());
    }

    /**
     * Creates a new {@code Slug} for the {@code organization}.
     */
    public static Slug forOrg(OrganizationId org) {
        checkNotNull(org);
        return create(org.getValue());
    }

    /**
     * Creates a new {@code Slug} with the specified {@code value}.
     */
    public static Slug create(String value) {
        checkNotEmptyOrBlank(value);
        return Slug.newBuilder()
                   .setValue(value)
                   .vBuild();
    }
}
