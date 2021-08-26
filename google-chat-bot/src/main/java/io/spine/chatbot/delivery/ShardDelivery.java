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

import io.spine.logging.Logging;
import io.spine.server.ServerEnvironment;
import io.spine.server.delivery.DeliveryStats;
import io.spine.server.delivery.ShardIndex;
import io.spine.server.delivery.ShardedRecord;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Delivers messages from a particular shard.
 *
 * <p>Wraps the {@link io.spine.server.delivery.Delivery#deliverMessagesFrom(ShardIndex)
 * Delivery#deliverMessagesFrom} with server environment-specific logging and provides helpers
 * that unifies the usage of the delivery.
 */
record ShardDelivery(ShardIndex shard) implements Logging {

    /**
     * Delivers the {@code message}.
     */
    static void deliver(ShardedRecord message) {
        checkNotNull(message);
        deliverFrom(message.shardIndex());
    }

    /**
     * Delivers messages from the {@code shard}.
     */
    private static void deliverFrom(ShardIndex shard) {
        checkNotNull(shard);
        var delivery = new ShardDelivery(shard);
        delivery.deliverNow();
    }

    private void deliverNow() {
        var server = ServerEnvironment.instance();
        var nodeId = server.nodeId()
                           .getValue();
        var indexValue = shard.getIndex();
        _trace().log("Delivering messages from the shard with index `%d`. NodeId=%s.",
                     indexValue, nodeId);
        var stats = server.delivery()
                          .deliverMessagesFrom(shard);
        if (stats.isPresent()) {
            DeliveryStats deliveryStats = stats.get();
            _trace().log("`%d` messages delivered from the shard with index `%s`. NodeId=%s.",
                         deliveryStats.deliveredCount(), indexValue, nodeId);
        } else {
            _trace().log("No messages delivered from the shard with index `%d`. NodeId=%s.",
                         indexValue, nodeId);
        }
    }
}
