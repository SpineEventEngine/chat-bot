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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Travis CI API query.
 *
 * @param <T>
 *         type of the expected query execution response
 */
public class Query<T extends TravisResponse> {

    private final Class<T> responseType;
    private final String request;

    /**
     * Creates a new API query with the specified {@code request}.
     */
    Query(String request, Class<T> responseType) {
        this.request = checkNotNull(request);
        this.responseType = checkNotNull(responseType);
    }

    /**
     * Returns query REST request URL.
     */
    String request() {
        return request;
    }

    /**
     * Returns query response type.
     */
    Class<T> responseType() {
        return responseType;
    }

    /**
     * Encodes passed value using {@link URLEncoder} and standard
     * {@link StandardCharsets#UTF_8 UTF_8} charset.
     */
    static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Query)) {
            return false;
        }
        Query<?> query = (Query<?>) o;
        return Objects.equal(responseType, query.responseType) &&
                Objects.equal(request, query.request);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(responseType, request);
    }

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this)
                .add("request", request)
                .add("responseType", responseType)
                .toString();
    }
}