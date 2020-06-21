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

import io.spine.chatbot.google.chat.Space;
import io.spine.chatbot.google.chat.SpaceId;
import io.spine.chatbot.google.chat.command.RegisterSpace;
import io.spine.chatbot.google.chat.event.SpaceRegistered;
import io.spine.chatbot.google.chat.incoming.SpaceType;
import io.spine.chatbot.google.chat.incoming.event.BotAddedToSpace;
import io.spine.core.External;
import io.spine.logging.Logging;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;
import io.spine.server.event.React;

/**
 * A room or direct message chat in the Google Chat.
 *
 * <p>Whenever the ChatBot is added to the space, the space is registered in the context.
 */
final class SpaceAggregate extends Aggregate<SpaceId, Space, Space.Builder> implements Logging {

    /**
     * Registers a new space when the ChatBot is added to the space.
     */
    @React
    SpaceRegistered on(@External BotAddedToSpace e) {
        var space = e.getEvent()
                     .getSpace();
        return SpaceRegistered
                .newBuilder()
                .setDisplayName(space.getDisplayName())
                .setThreaded(isThreaded(space))
                .setId(e.getSpaceId())
                .vBuild();
    }

    /**
     * Registers the space in the context.
     */
    @Assign
    SpaceRegistered handle(RegisterSpace c) {
        _info().log("Registering space `%s`.", idAsString());
        var result = SpaceRegistered
                .newBuilder()
                .setId(c.getId())
                .setSingleUserBotDm(c.getSingleUserBotDm())
                .setThreaded(c.getThreaded())
                .setDisplayName(c.getDisplayName())
                .vBuild();
        return result;
    }

    @Apply
    private void on(SpaceRegistered e) {
        builder().setDisplayName(e.getDisplayName())
                 .setSingleUserBotDm(e.getSingleUserBotDm())
                 .setThreaded(e.getThreaded());
    }

    private static boolean isThreaded(io.spine.chatbot.google.chat.incoming.Space space) {
        return space.getType() == SpaceType.ROOM;
    }
}
