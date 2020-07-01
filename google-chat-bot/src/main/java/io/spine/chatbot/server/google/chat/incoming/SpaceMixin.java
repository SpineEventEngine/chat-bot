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

package io.spine.chatbot.server.google.chat.incoming;

import io.spine.annotation.GeneratedMixin;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.incoming.SpaceOrBuilder;
import io.spine.chatbot.google.chat.incoming.SpaceType;

import static io.spine.chatbot.server.google.chat.GoogleChatIdentifier.space;

/**
 * Provides utility helpers for the {@link io.spine.chatbot.google.chat.incoming.Space Space} type.
 */
@GeneratedMixin
public interface SpaceMixin extends SpaceOrBuilder {

    /**
     * Determines whether the space is threaded.
     *
     * @return {@code true} if the space is threaded, {@code false} otherwise
     */
    default boolean isThreaded() {
        return getType() == SpaceType.ROOM;
    }

    /**
     * Returns the space ID.
     */
    default SpaceId id() {
        return space(getName());
    }
}
