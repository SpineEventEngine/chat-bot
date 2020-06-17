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

import com.google.common.annotations.VisibleForTesting;
import io.micronaut.runtime.Micronaut;
import io.spine.chatbot.server.github.GitHubContext;
import io.spine.chatbot.server.google.chat.GoogleChatContext;
import io.spine.server.Server;

import java.io.IOException;

/** The entry point to the Google Chat Bot application. **/
public final class Application {

    static final String SERVER_NAME = "ChatBotServer";

    /** Prevents direct instantiation. **/
    private Application() {
    }

    /** Starts the application. **/
    public static void main(String[] args) {
        initializeSpine();
        Micronaut.run(Application.class, args);
    }

    /** Initializes Spine server environment and starts Spine {@link Server}. **/
    @VisibleForTesting
    static void initializeSpine() {
        ChatBotServerEnvironment.initializeEnvironment();
        Server server = Server
                .inProcess(SERVER_NAME)
                .add(GitHubContext.newBuilder())
                .add(GoogleChatContext.newBuilder())
                .build();
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
