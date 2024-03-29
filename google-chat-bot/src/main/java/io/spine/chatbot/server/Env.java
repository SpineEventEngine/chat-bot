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

import com.google.cloud.datastore.DatastoreOptions;
import io.spine.chatbot.delivery.LocalDelivery;
import io.spine.environment.Environment;
import io.spine.environment.EnvironmentType;
import io.spine.environment.Production;
import io.spine.server.ServerEnvironment;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.datastore.DatastoreStorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;

/**
 * Initializes the {@link ServerEnvironment}.
 *
 * <p>Configures the {@link StorageFactory} depending on the current
 * {@linkplain Environment environment}.
 * Uses the Datastore storage factory for the production mode and in-memory storage for tests.
 *
 * <p>Configures the inbox delivery through the Datastore work registry while
 * in Production environment, otherwise uses local synchronous delivery.
 */
final class Env {

    /**
     * Prevents instantiation of this utility class.
     */
    private Env() {
    }

    /**
     * Initializes {@link ServerEnvironment} for ChatBot.
     */
    static void init() {
        //TODO:2020-06-21:yuri-sergiichuk: switch to io.spine.chatbot.delivery.DistributedDelivery
        // for Production environment after implementing the delivery strategy.
        // see https://github.com/SpineEventEngine/chat-bot/issues/5.
        var env = Environment.instance();
        ServerEnvironment
                .when(env.type())
                .use(InMemoryTransportFactory.newInstance())
                .use(LocalDelivery.instance)
                .useStorageFactory(Env::determineStorage);
    }

    private static StorageFactory determineStorage(Class<? extends EnvironmentType> env) {
        if (Production.class.equals(env)) {
            return dsStorageFactory();
        }
        return InMemoryStorageFactory.newInstance();
    }

    private static DatastoreStorageFactory dsStorageFactory() {
        var datastore = DatastoreOptions
                .getDefaultInstance()
                .getService();
        return DatastoreStorageFactory
                .newBuilder()
                .setDatastore(datastore)
                .build();
    }
}
