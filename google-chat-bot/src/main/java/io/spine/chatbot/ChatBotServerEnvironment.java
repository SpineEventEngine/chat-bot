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

import com.google.cloud.datastore.DatastoreOptions;
import io.spine.base.Environment;
import io.spine.chatbot.delivery.DeliveryFactory;
import io.spine.server.ServerEnvironment;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.datastore.DatastoreStorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;

/**
 * ChatBot server environment definition.
 *
 * <p>Initializes the {@link ServerEnvironment}.
 *
 * <p>Configures the {@link StorageFactory} based on
 * the current {@link Environment} — Datastore-based for Production and in-memory-based for tests.
 *
 * <p>Configures the inbox delivery through the Datastore work registry while
 * in Production environment, otherwise uses local synchronous delivery.
 *
 * @see DeliveryFactory
 */
final class ChatBotServerEnvironment {

    /**
     * Prevents instantiation of this utility class.
     */
    private ChatBotServerEnvironment() {
    }

    /**
     * Initializes {@link ServerEnvironment} for ChatBot.
     */
    static void initializeEnvironment() {
        var se = ServerEnvironment.instance();
        var environment = Environment.instance();
        var storageFactory = dsStorageFactory();
        var deliveryFactory = DeliveryFactory.instance(environment, storageFactory);
        var delivery = deliveryFactory.delivery();
        se.configureStorage(storageFactory);
        se.configureTransport(InMemoryTransportFactory.newInstance());
        se.configureDelivery(delivery);
    }

    private static DatastoreStorageFactory dsStorageFactory() {
        var datastore = DatastoreOptions.getDefaultInstance()
                                        .getService();
        return DatastoreStorageFactory
                .newBuilder()
                .setDatastore(datastore)
                .build();
    }
}
