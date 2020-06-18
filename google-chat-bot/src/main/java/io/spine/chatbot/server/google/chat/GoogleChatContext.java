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

package io.spine.chatbot.server.google.chat;

import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;

/**
 * Provides {@link BoundedContextBuilder} for the Google Chat context.
 */
public final class GoogleChatContext {

    /** The name of the Context. **/
    static final String NAME = "GoogleChat";

    private final BoundedContextBuilder contextBuilder;

    /** Prevents instantiation of this utility class. **/
    private GoogleChatContext() {
        this.contextBuilder = configureContextBuilder();
    }

    /** Returns the context builder associated with the Google Chat context. **/
    public BoundedContextBuilder contextBuilder() {
        return this.contextBuilder;
    }

    /** Creates a new instance of the Google Chat context builder. **/
    private static BoundedContextBuilder configureContextBuilder() {
        return BoundedContext
                .singleTenant(NAME)
                .add(SpaceAggregate.class)
                .add(new ThreadAggregateRepository())
                .add(new ThreadChatProcessRepository());
    }

    /** Creates a new builder of the Google Chat context. **/
    public static Builder newBuilder() {
        return new Builder();
    }

    /** A Builder for configuring Google Chat context. **/
    public static final class Builder {

        //TODO:2020-06-18:ysergiichuk: add ability to configure GoogleChatClient

        private Builder() {
        }

        /**
         * Finishes configuration of the context and builds a new instance.
         */
        public GoogleChatContext build() {
            return new GoogleChatContext();
        }
    }
}
