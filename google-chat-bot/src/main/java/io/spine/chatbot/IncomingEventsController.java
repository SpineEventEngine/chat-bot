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
import io.spine.chatbot.client.ChatBotClient;
import io.spine.chatbot.google.chat.command.RegisterSpace;
import io.spine.chatbot.google.chat.event.SpaceRegistered;
import io.spine.chatbot.google.chat.incoming.ChatEvent;
import io.spine.chatbot.google.chat.incoming.Space;
import io.spine.chatbot.google.chat.incoming.SpaceType;
import io.spine.chatbot.server.google.chat.Identifiers;
import io.spine.json.Json;
import io.spine.logging.Logging;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.micronaut.http.MediaType.APPLICATION_JSON;

/**
 * A REST controller for handling incoming events from Google Chat.
 */
@Controller("/chat")
public class IncomingEventsController implements Logging {

    /**
     * Processes an incoming Google Chat event.
     *
     * <p>When a bot is added to a new space, registers the space in the system.
     */
    @Post(value = "/incoming/event", consumes = APPLICATION_JSON)
    public String on(@Body PubsubPushNotification pushNotification) {
        var message = pushNotification.getMessage();
        var chatEventJson = decodeBase64Json(message.getData());
        _debug().log("Received a new chat event: %s", chatEventJson);
        ChatEvent chatEvent = Json.fromJson(chatEventJson, ChatEvent.class);
        var client = ChatBotClient.inProcessClient(Application.SERVER_NAME);
        switch (chatEvent.getType()) {
            case MESSAGE:
                _info().log("Processing user message.");
                break;
            case ADDED_TO_SPACE:
                var space = chatEvent.getSpace();
                _info().log("Bot added to space `%s` (%s).",
                            space.getDisplayName(), space.getName());
                onBotAddedToSpace(space, client);
                break;
            case REMOVED_FROM_SPACE:
            case CARD_CLICKED:
            case UNRECOGNIZED:
            case ET_UNKNOWN:
                _debug().log("Unsupported chat event type received: %s", chatEvent.getType());
                break;
        }
        return "OK";
    }

    private static void onBotAddedToSpace(Space space, ChatBotClient client) {
        var registerSpace = RegisterSpace
                .newBuilder()
                .setDisplayName(space.getDisplayName())
                .setThreaded(isThreaded(space))
                .setId(Identifiers.newSpaceId(space.getName()))
                .vBuild();
        client.postSyncCommand(registerSpace, SpaceRegistered.class);
    }

    private static boolean isThreaded(Space space) {
        return space.getType() == SpaceType.ROOM;
    }

    private static String decodeBase64Json(ByteString encoded) {
        var decodedBytes = Base64.getDecoder()
                                 .decode(encoded.toByteArray());
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
