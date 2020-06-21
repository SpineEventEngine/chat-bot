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

package io.spine.chatbot;

import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubPushNotification;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.spine.chatbot.google.chat.incoming.ChatEvent;
import io.spine.chatbot.google.chat.incoming.User;
import io.spine.core.UserId;
import io.spine.json.Json;
import io.spine.logging.Logging;
import io.spine.server.integration.ThirdPartyContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.micronaut.http.MediaType.APPLICATION_JSON;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A REST controller for handling incoming events from Google Chat.
 */
@Controller("/chat")
final class IncomingEventsController implements Logging {

    private static final String CONTEXT_NAME = "IncomingChatEvents";

    /**
     * Processes an incoming Google Chat event.
     *
     * <p>When a bot is added to a new space, registers the space in the system.
     */
    @Post(value = "/incoming/event", consumes = APPLICATION_JSON)
    String on(@Body PubsubPushNotification pushNotification) {
        var message = pushNotification.getMessage();
        var chatEventJson = decodeBase64Json(message.getData());
        _debug().log("Received a new chat event: %s", chatEventJson);
        ChatEvent chatEvent = Json.fromJson(chatEventJson, ChatEvent.class);
        var actor = eventActor(chatEvent.getUser());
        try (ThirdPartyContext incomingEvents = ThirdPartyContext.singleTenant(CONTEXT_NAME)) {
            incomingEvents.emittedEvent(chatEvent, actor);
        } catch (Exception e) {
            throw newIllegalStateException("Unable to handle incoming Google Chat event.", e);
        }
        return "OK";
    }

    private static UserId eventActor(User user) {
        return UserId.newBuilder()
                     .setValue(user.getName())
                     .vBuild();
    }

    private static String decodeBase64Json(ByteString encoded) {
        var decodedBytes = Base64.getDecoder()
                                 .decode(encoded.toByteArray());
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
