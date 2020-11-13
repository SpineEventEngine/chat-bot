/*
 * Copyright 2020, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.chatbot;

import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.runtime.event.annotation.EventListener;
import io.spine.chatbot.google.chat.incoming.ChatEvent;
import io.spine.chatbot.google.chat.incoming.User;
import io.spine.chatbot.google.chat.incoming.event.ChatEventReceived;
import io.spine.core.UserId;
import io.spine.json.Json;
import io.spine.logging.Logging;
import io.spine.pubsub.PubsubPushRequest;
import io.spine.server.integration.ThirdPartyContext;

import static io.micronaut.http.MediaType.APPLICATION_JSON;

/**
 * A REST controller for handling incoming events from Google Chat.
 */
@Controller("/chat")
final class IncomingEventsController implements Logging {

    private static final String GOOGLE_CHAT_SERVER_CONTEXT_NAME = "GoogleChatServer";
    private static final ThirdPartyContext GOOGLE_CHAT_SERVER =
            ThirdPartyContext.singleTenant(GOOGLE_CHAT_SERVER_CONTEXT_NAME);

    /**
     * Processes an incoming Google Chat event.
     *
     * <p>Dispatches the event using {@link ThirdPartyContext}.
     */
    @Post(value = "/incoming/event", consumes = APPLICATION_JSON)
    String on(@Body PubsubPushRequest pushRequest) {
        var message = pushRequest.getMessage();
        var chatEventJson = message.getData()
                                   .toStringUtf8();
        _debug().log("Received a new chat event:%n%s", chatEventJson);
        ChatEvent chatEvent = Json.fromJson(chatEventJson, ChatEvent.class);
        var actor = eventActor(chatEvent.getUser());
        var chatEventReceived = ChatEventReceived
                .newBuilder()
                .setEvent(chatEvent)
                .vBuild();
        GOOGLE_CHAT_SERVER.emittedEvent(chatEventReceived, actor);
        return "OK";
    }

    private static UserId eventActor(User user) {
        return UserId
                .newBuilder()
                .setValue(user.getName())
                .vBuild();
    }

    /**
     * Cleans up resources of the {@link #GOOGLE_CHAT_SERVER context}.
     */
    @EventListener
    void on(ShutdownEvent event) {
        _info().log("Closing `%s` third-party context.", GOOGLE_CHAT_SERVER_CONTEXT_NAME);
        try {
            GOOGLE_CHAT_SERVER.close();
        } catch (Exception e) {
            _error().withCause(e)
                    .log("Unable to gracefully close `%s` context.",
                         GOOGLE_CHAT_SERVER_CONTEXT_NAME);
        }
    }
}
