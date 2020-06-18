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

    /** Delivers the {@code message}. **/
    static void deliver(ShardedRecord message) {
        deliverFrom(message.shardIndex());
    }

    /** Delivers messages from the {@code shard}. **/
    static void deliverFrom(ShardIndex shard) {
        var delivery = new ShardDelivery(shard);
        delivery.deliverNow();
    }

    private void deliverNow() {
        var server = ServerEnvironment.instance();
        var nodeId = server.nodeId().getValue();
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
