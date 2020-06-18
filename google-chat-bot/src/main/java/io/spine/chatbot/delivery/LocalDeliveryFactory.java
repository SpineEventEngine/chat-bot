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

import com.google.protobuf.util.Durations;
import io.spine.server.delivery.CatchUpStorage;
import io.spine.server.delivery.Delivery;
import io.spine.server.delivery.InboxStorage;
import io.spine.server.delivery.UniformAcrossAllShards;
import io.spine.server.delivery.memory.InMemoryShardedWorkRegistry;
import io.spine.server.storage.memory.InMemoryCatchUpStorage;
import io.spine.server.storage.memory.InMemoryInboxStorage;

/**
 * A {@link Delivery} factory that creates deliveries for local or test environments.
 */
final class LocalDeliveryFactory implements DeliveryFactory {

    /** A singleton instance of the local delivery factory. **/
    static final LocalDeliveryFactory instance = new LocalDeliveryFactory();

    /** Prevents instantiation of this class. **/
    private LocalDeliveryFactory() {
    }

    /**
     * Creates a new instance of an in-memory local delivery.
     */
    @Override
    public Delivery delivery() {
        var delivery = Delivery
                .newBuilder()
                .setInboxStorage(singleTenantInboxStorage())
                .setCatchUpStorage(singleTenantCatchupStorage())
                .setWorkRegistry(new InMemoryShardedWorkRegistry())
                .setStrategy(UniformAcrossAllShards.singleShard())
                .setDeduplicationWindow(Durations.fromSeconds(0))
                .build();
        delivery.subscribe(ShardDelivery::deliver);
        return delivery;
    }

    @SuppressWarnings("TestOnlyProblems") // we do want the in-memory delivery in local-dev env
    private static InboxStorage singleTenantInboxStorage() {
        return new InMemoryInboxStorage(false);
    }

    @SuppressWarnings("TestOnlyProblems") // we do want the in-memory delivery in local-dev env
    private static CatchUpStorage singleTenantCatchupStorage() {
        return new InMemoryCatchUpStorage(false);
    }
}
