/*
 * Copyright 2021, TeamDev. All rights reserved.
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

package io.spine.chatbot.server;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.concurrent.LazyInit;
import io.spine.logging.Logging;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A ChatBot application's Spine server.
 *
 * <p>Abstracts working with Spine's {@link io.spine.server.Server server}.
 */
public final class Server implements Logging {

    /**
     * The name of the GRPC {@link io.spine.server.Server Server}.
     */
    private static final String SERVER_NAME = "ChatBotServer";

    private final ImmutableSet<ContextBuilderAware> contexts;

    @LazyInit
    private io.spine.server.@MonotonicNonNull Server grpcServer;

    private Server(ImmutableSet<ContextBuilderAware> contexts) {
        this.contexts = contexts;
    }

    /**
     * Creates a new in-process GRPC server with the supplied {@code contexts}.
     */
    public static Server withContexts(ContextBuilderAware... contexts) {
        checkNotNull(contexts);
        checkArgument(
                contexts.length > 0,
                "At least a single Bounded Context is required."
        );
        return new Server(ImmutableSet.copyOf(contexts));
    }

    /**
     * Returns the name of the server.
     */
    public static String name() {
        return SERVER_NAME;
    }

    /**
     * Starts the server.
     *
     * <p>Performs {@linkplain #init() initialization} of the server if it was not
     * previously initialized.
     */
    public void start() {
        if (grpcServer == null) {
            init();
        }
        try {
            _config().log("Starting GRPC server.");
            grpcServer.start();
            Runtime.getRuntime()
                   .addShutdownHook(ShutdownHook.newInstance(grpcServer));
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Unable to start Spine GRPC server `%s`.", SERVER_NAME
            );
        }
    }

    /**
     * Initializes the server and its {@linkplain Env environment}.
     */
    public void init() {
        _config().log("Initializing server environment.");
        Env.init();
        _config().log("Bootstrapping server.");
        var serverBuilder = io.spine.server.Server.inProcess(SERVER_NAME);
        for (var contextAware : contexts) {
            serverBuilder.add(contextAware.builder());
        }
        grpcServer = serverBuilder.build();
    }

    /**
     * Gracefully stops the {@link #server}.
     */
    private record ShutdownHook(io.spine.server.Server server) implements Runnable, Logging {

        private static Thread newInstance(io.spine.server.Server server) {
            checkNotNull(server);
            return new Thread(new ShutdownHook(server));
        }

        @Override
        public void run() {
            _info().log("Shutting down the GRPC server.");
            server.shutdown();
        }
    }
}
