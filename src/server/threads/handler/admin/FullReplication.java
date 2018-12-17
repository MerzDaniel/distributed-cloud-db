package server.threads.handler.admin;

import lib.message.AdminMessages.FullReplicationMsg;
import lib.message.KVAdminMessage;
import lib.message.Messaging;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;
import server.kv.KeyValueStore;
import server.threads.handler.AdminMessageHandler;

import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.stream.Collectors;

public final class FullReplication {
    static Logger logger = LogManager.getLogger(FullReplication.class);

    public static KVAdminMessage doFullReplication(FullReplicationMsg message, ServerState state) {
        HashMap<Long, Messaging> messagingHashMap = new HashMap<>();
        ServerData targetServer;
        ServerData srcData;
        try {
            targetServer = state.meta.findKvServerByName(message.targetServerName);
            srcData = state.meta.findKvServerByName(message.srcDataServerName);
        } catch (KVServerNotFoundException e) {
            logger.warn("Tried to replicate to non existing server");
            return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_ERROR);
        }

        KeyValueStore db = state.dbProvider.getDb(srcData);
        IntSummaryStatistics errorCount = db.retrieveAllData().parallel().map((d) -> {
            Long currentThreadId = Thread.currentThread().getId();
            try {
                if (messagingHashMap.get(currentThreadId) == null) {
                    messagingHashMap.put(currentThreadId, new Messaging());
                    messagingHashMap.get(currentThreadId).connect(targetServer);
                }

                KVAdminMessage replicateMsg = new KVAdminMessage(
                        KVAdminMessage.StatusType.PUT_REPLICATE,
                        d.getKey(),
                        d.getValue()
                );
                messagingHashMap.get(currentThreadId).sendMessage(replicateMsg);
                KVAdminMessage response = (KVAdminMessage) messagingHashMap.get(currentThreadId).readMessage();
                if (response.status != KVAdminMessage.StatusType.PUT_REPLICATE_SUCCESS)
                    throw new Exception("Problem during replicate");

            } catch (Exception e) {
                messagingHashMap.remove(currentThreadId);
                logger.debug("Problem during replication", e);
                return 1;
            }
            return 0;
        }).collect(Collectors.summarizingInt(x -> (int) x));

        if (errorCount.getSum() > 0) {
            logger.warn(String.format(
                    "Errors during replication from %s (data: %s) to %s",
                    state.currentServerServerData.getName(),
                    srcData.getName(),
                    targetServer.getName())
            );
            return new KVAdminMessage(KVAdminMessage.StatusType.FULL_REPLICATE_ERROR);
        }

        return new KVAdminMessage(KVAdminMessage.StatusType.FULL_REPLICATE_SUCCESS);
    }
}
