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

import io.micronaut.runtime.Micronaut;
import io.spine.chatbot.server.Server;
import io.spine.chatbot.server.github.GitHubContext;
import io.spine.chatbot.server.google.chat.GoogleChatContext;
import io.spine.logging.Logging;

/**
 * The entry point to the Spine ChatBot application.
 *
 * <p>The application exposes a number of REST endpoints accessible for the clients such as:
 *
 * <ul>
 *     <li>{@code /chat/incoming/event} — handles incoming events from the Google Chat space;
 *     <li>{@code /repositories/check} — triggers checking of the repositories build statuses.
 * </ul>
 *
 * @see IncomingEventsController
 * @see RepositoriesController
 **/
public final class Application implements Logging {

    static {
        useLog4j2FloggerBackend();
    }

    /**
     * Prevents direct instantiation.
     */
    private Application() {
    }

    /**
     * Starts the application.
     *
     * <p>Performs bounded contexts initialization, starts Spine {@link Server} and runs
     * the {@link Micronaut}.
     */
    public static void main(String[] args) {
        var application = new Application();
        application.start();
    }

    private void start() {
        Server.withContexts(GitHubContext.newInstance(), GoogleChatContext.newInstance())
              .start();
        _config().log("Starting Micronaut application.");
        Micronaut.run(Application.class);
        _info().log("Application successfully started. Waiting for incoming requests.");
    }

    /**
     * Configures Log4j2 as the <a href="https://github.com/google/flogger">Flogger</a> backend.
     */
    private static void useLog4j2FloggerBackend() {
        System.setProperty(
                "flogger.backend_factory",
                "com.google.common.flogger.backend.log4j2.Log4j2BackendFactory#getInstance"
        );
    }
}
