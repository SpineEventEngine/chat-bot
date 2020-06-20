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

import io.spine.logging.Logging;
import io.spine.server.ServerEnvironment;
import io.spine.server.delivery.DeliveryStats;
import io.spine.server.delivery.ShardIndex;
import io.spine.server.delivery.ShardedRecord;

/**
 * Delivery of messages from a particular shard.
 */
final class ShardDelivery implements Logging {

    private final ShardIndex shard;

    private ShardDelivery(ShardIndex shard) {
        this.shard = shard;
    }

    /**
     * Delivers the {@code message}.
     */
    static void deliver(ShardedRecord message) {
        deliverFrom(message.shardIndex());
    }

    /**
     * Delivers messages from the {@code shard}.
     */
    static void deliverFrom(ShardIndex shard) {
        var delivery = new ShardDelivery(shard);
        delivery.deliverNow();
    }

    private void deliverNow() {
        var server = ServerEnvironment.instance();
        var nodeId = server.nodeId()
                           .getValue();
        var indexValue = shard.getIndex();
        _trace().log("Delivering messages from shard with index `%d`. NodeId=%s.",
                     indexValue, nodeId);
        var stats = server.delivery().deliverMessagesFrom(shard);
        if (stats.isPresent()) {
            DeliveryStats deliveryStats = stats.get();
            _trace().log("`%d` messages delivered from shard with index `%s`. NodeId=%s.",
                         deliveryStats.deliveredCount(), indexValue, nodeId);
        } else {
            _trace().log("No messages delivered from shard with index `%d`. NodeId=%s.",
                         indexValue, nodeId);
        }
    }
}
