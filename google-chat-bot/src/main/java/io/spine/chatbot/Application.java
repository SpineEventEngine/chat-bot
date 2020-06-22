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
import com.google.common.flogger.FluentLogger;
import io.micronaut.runtime.Micronaut;
import io.spine.chatbot.server.github.GitHubContext;
import io.spine.chatbot.server.google.chat.GoogleChatContext;
import io.spine.logging.Logging;
import io.spine.server.Server;

import java.io.IOException;

import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * The entry point to the Google Chat Bot application.
 *
 * <p>The application itself exposes a number of REST endpoints accessible for the clients such as:
 *
 * <ul>
 *     <li>{@code /chat/incoming/event} — handles incoming events from the Google Chat space.
 *     <li>{@code /repositories/check} — triggers checking of the repositories build statuses.
 * </ul>
 *
 * @see IncomingEventsController
 * @see RepositoriesController
 **/
public final class Application {

    static {
        System.setProperty(
                "flogger.backend_factory",
                "com.google.common.flogger.backend.log4j2.Log4j2BackendFactory#getInstance"
        );
    }

    private static final FluentLogger LOGGER = Logging.loggerFor(Application.class);

    /** Name of the GRPC {@link Server}. **/
    static final String SERVER_NAME = "ChatBotServer";

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
        LOGGER.atFine()
              .log("Starting Spine ChatBot application.");
        initializeSpine();
        Micronaut.run(Application.class, args);
    }

    /**
     * Initializes Spine server environment and starts Spine {@link Server}.
     */
    private static void initializeSpine() {
        LOGGER.atConfig()
              .log("Initializing server environment.");
        ChatBotServerEnvironment.initializeEnvironment();
        var gitHubContext = GitHubContext
                .newBuilder()
                .build();
        var googleChatContext = GoogleChatContext
                .newBuilder()
                .build();
        startSpineServer(gitHubContext, googleChatContext);
    }

    /**
     * Starts Spine in-process server.
     */
    @VisibleForTesting
    static void startSpineServer(GitHubContext gitHubContext, GoogleChatContext googleChatContext) {
        LOGGER.atConfig()
              .log("Starting server.");
        Server server = Server
                .inProcess(SERVER_NAME)
                .add(gitHubContext.contextBuilder())
                .add(googleChatContext.contextBuilder())
                .build();
        try {
            server.start();
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Unable to start Spine GRPC server `%s`.", SERVER_NAME
            );
        }
    }
}
