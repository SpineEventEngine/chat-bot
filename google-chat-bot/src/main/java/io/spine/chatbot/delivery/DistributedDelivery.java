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

package io.spine.chatbot.delivery;

import io.spine.server.delivery.Delivery;
import io.spine.server.delivery.ShardedWorkRegistry;
import io.spine.server.delivery.UniformAcrossAllShards;
import io.spine.server.storage.datastore.DatastoreStorageFactory;
import io.spine.server.storage.datastore.DsShardedWorkRegistry;

/**
 * Delivers messages using Datastore as the underlying storage.
 *
 * <p>The delivery is based on the {@link DatastoreStorageFactory Datastore} and uses
 * {@link DsShardedWorkRegistry} as the {@linkplain ShardedWorkRegistry work registry}.
 */
final class DistributedDelivery {

    /** The number of shards used for the signal delivery. **/
    private static final int NUMBER_OF_SHARDS = 50;

    /**
     * Prevents instantiation of this utility class.
     */
    private DistributedDelivery() {
    }

    /**
     * Creates a new Datastore-based delivery using the supplied Datastore {@code storageFactory}.
     *
     * <p>Assigns the targets uniformly across shards. Configures the inbox storage
     * to be single-tenant.
     */
    public static Delivery instance(DatastoreStorageFactory storageFactory) {
        var workRegistry = new DsShardedWorkRegistry(storageFactory);
        var inboxStorage = storageFactory.createInboxStorage(false);
        var delivery = Delivery
                .newBuilder()
                .setStrategy(UniformAcrossAllShards.forNumber(NUMBER_OF_SHARDS))
                .setWorkRegistry(workRegistry)
                .setInboxStorage(inboxStorage)
                .build();
        return delivery;
    }
}
