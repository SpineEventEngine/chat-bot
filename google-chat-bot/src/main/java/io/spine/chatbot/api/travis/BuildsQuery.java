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

package io.spine.chatbot.api.travis;

/**
 * A branch builds query to the Travis CI API.
 *
 * @see <a href="https://developer.travis-ci.com/resource/branch#find">Find branch build</a>
 */
public final class BuildsQuery extends Query<RepoBranchBuildResponse> {

    private BuildsQuery(String request) {
        super(request, RepoBranchBuildResponse.class);
    }

    /**
     * Creates a query for a repository with the specified {@code slug}.
     *
     * <p>Requests the latest build from the {@code master} branch.
     */
    public static BuildsQuery forRepo(String slug) {
        var encodedSlug = encode(slug);
        var request = "/repo/"
                + encodedSlug
                + "/branch/master?&include=build.commit,build.created_by";
        return new BuildsQuery(request);
    }
}
