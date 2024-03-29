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

package io.spine.chatbot.delivery;

import com.google.protobuf.util.Durations;
import io.spine.server.delivery.Delivery;
import io.spine.server.delivery.UniformAcrossAllShards;
import io.spine.server.delivery.memory.InMemoryShardedWorkRegistry;
import io.spine.server.storage.memory.InMemoryStorageFactory;

/**
 * A {@link Delivery} factory that creates deliveries for local or test environments.
 */
public final class LocalDelivery {

    /** A singleton instance of the local delivery. **/
    public static final Delivery instance = delivery();

    /**
     * Prevents instantiation of this class.
     */
    private LocalDelivery() {
    }

    /**
     * Creates a new instance of an in-memory local delivery.
     */
    private static Delivery delivery() {
        var storages = InMemoryStorageFactory.newInstance();
        var delivery = Delivery
                .newBuilder()
                .setInboxStorage(storages.createInboxStorage(false))
                .setCatchUpStorage(storages.createCatchUpStorage(false))
                .setWorkRegistry(new InMemoryShardedWorkRegistry())
                .setStrategy(UniformAcrossAllShards.singleShard())
                .setDeduplicationWindow(Durations.fromSeconds(0))
                .build();
        delivery.subscribe(ShardDelivery::deliver);
        return delivery;
    }
}
