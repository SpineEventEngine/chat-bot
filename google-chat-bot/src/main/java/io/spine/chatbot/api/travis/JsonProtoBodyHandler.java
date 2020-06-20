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

import com.google.protobuf.Message;
import io.spine.json.Json;

import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link HttpResponse.BodyHandler BodyHandler} for JSON Protobuf messages.
 *
 * <p>The handler converts input JSON strings into the Protobuf messages relying on the
 * Spine {@link Json} conversion functionality.
 *
 * @param <T>
 *         the Protobuf message supplied in the response body
 */
final class JsonProtoBodyHandler<T extends Message> implements HttpResponse.BodyHandler<T> {

    private final Class<T> type;

    private JsonProtoBodyHandler(Class<T> type) {
        this.type = type;
    }

    /**
     * Creates a body handler for a specified Protobuf message.
     */
    static <T extends Message> JsonProtoBodyHandler<T> jsonBodyHandler(Class<T> type) {
        checkNotNull(type);
        return new JsonProtoBodyHandler<>(type);
    }

    @Override
    public HttpResponse.BodySubscriber<T> apply(ResponseInfo responseInfo) {
        return BodySubscribers.mapping(BodySubscribers.ofString(StandardCharsets.UTF_8),
                                       this::parseJson);
    }

    private T parseJson(String json) {
        return Json.fromJson(json, type);
    }
}
