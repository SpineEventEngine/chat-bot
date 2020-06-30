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

package io.spine.net;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static io.spine.util.Preconditions2.checkPositive;
import static java.lang.String.format;

/**
 * Static factories for creating project-specific {@link Url}s.
 */
public final class MoreUrls {

    private static final String TRAVIS_GITHUB_ENDPOINT = "https://travis-ci.com/github";
    private static final String GITHUB = "https://github.com";

    /**
     * Prevents instantiation of this utility class.
     */
    private MoreUrls() {
    }

    /**
     * Creates a new Travis CI build URL for a build with the specified {@code buildId} of
     * the repository with the specified {@code slug}.
     */
    public static Url travisBuildUrlFor(String slug, long buildId) {
        checkNotEmptyOrBlank(slug);
        checkPositive(buildId);
        var spec = format("%s/%s/builds/%d", TRAVIS_GITHUB_ENDPOINT, slug, buildId);
        return Urls.create(spec);
    }

    /**
     * Creates a new Travis CI URL.
     */
    public static Url travisUrlFor(String slug) {
        checkNotNull(slug);
        var spec = format("%s/%s", TRAVIS_GITHUB_ENDPOINT, slug);
        return Urls.create(spec);
    }

    /**
     * Creates a new GitHub URL.
     */
    public static Url githubUrlFor(String slug) {
        checkNotNull(slug);
        var spec = format("%s/%s", GITHUB, slug);
        return Urls.create(spec);
    }
}
