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
import com.google.errorprone.annotations.concurrent.LazyInit;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.runtime.Micronaut;
import io.spine.chatbot.server.github.GitHubContext;
import io.spine.chatbot.server.google.chat.GoogleChatContext;
import io.spine.logging.Logging;
import io.spine.server.Server;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * The entry point to the Spine ChatBot application.
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
public final class Application implements Logging {

    static {
        System.setProperty(
                "flogger.backend_factory",
                "com.google.common.flogger.backend.log4j2.Log4j2BackendFactory#getInstance"
        );
    }

    /** Name of the GRPC {@link Server}. **/
    static final String SERVER_NAME = "ChatBotServer";

    @LazyInit
    private @MonotonicNonNull Server server;

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
        application.start(args);
    }

    /**
     * Starts the application.
     *
     * <p>Performs bounded contexts initialization, starts GRPC {@link Server} and runs
     * the {@link Micronaut}.
     */
    private void start(String[] args) {
        _config().log("Initializing server environment.");
        ChatBotServerEnvironment.init();
        _config().log("Setting up bounded contexts.");
        var gitHubContext = GitHubContext
                .newBuilder()
                .build();
        var googleChatContext = GoogleChatContext
                .newBuilder()
                .build();
        _config().log("Starting GRPC server.");
        server = startServer(gitHubContext, googleChatContext);
        _config().log("Starting Micronaut application.");
        var applicationContext = Micronaut.run(Application.class, args);
        applicationContext.registerSingleton(new Stopper(server));
    }

    /**
     * Starts in-process GRPC server.
     */
    @VisibleForTesting
    static Server startServer(GitHubContext gitHubContext, GoogleChatContext googleChatContext) {
        Server server = Server
                .inProcess(SERVER_NAME)
                .add(gitHubContext.builder())
                .add(googleChatContext.builder())
                .build();
        try {
            server.start();
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Unable to start Spine GRPC server `%s`.", SERVER_NAME
            );
        }
        return server;
    }

    /**
     * Gracefully stops the {@link #server}.
     */
    private static final class Stopper implements ApplicationEventListener<ShutdownEvent>, Logging {

        private final Server server;

        private Stopper(Server server) {
            this.server = checkNotNull(server);
        }

        @Override
        public void onApplicationEvent(ShutdownEvent event) {
            _info().log("Shutting down the application.");
            if (server != null) {
                server.shutdownAndWait();
            }
        }
    }
}
