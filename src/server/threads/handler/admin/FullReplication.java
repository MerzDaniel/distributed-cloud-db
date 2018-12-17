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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class FullReplication {
    static Logger logger = LogManager.getLogger(FullReplication.class);

    public static KVAdminMessage doFullReplication(FullReplicationMsg message, ServerState state) {
        ServerData targetServer;
        ServerData srcData;
        try {
            targetServer = state.meta.findKvServerByName(message.targetServerName);
            srcData = state.meta.findKvServerByName(message.srcDataServerName);
        } catch (KVServerNotFoundException e) {
            logger.warn("Tried to replicate to non existing server");
            return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_ERROR);
        }

        KeyValueStore sourceDb = state.dbProvider.getDb(srcData);

        List<Exception> combinedErrors = moveDataToExternalServer(targetServer, sourceDb);

        if (combinedErrors.size() > 0) {
            logger.warn(String.format(
                    "Errors during replication from %s (data: %s) to %s",
                    state.currentServerServerData.getName(),
                    srcData.getName(),
                    targetServer.getName()));
            combinedErrors.forEach(e -> {
                logger.warn(e.getMessage());
                logger.warn(Arrays.stream(e.getStackTrace())
                        .map(s->"        "+s.toString())
                        .collect(Collectors.joining("\n")));
            });

            return new KVAdminMessage(KVAdminMessage.StatusType.FULL_REPLICATE_ERROR);
        }

        return new KVAdminMessage(KVAdminMessage.StatusType.FULL_REPLICATE_SUCCESS);
    }

    private static List<Exception> moveDataToExternalServer(ServerData targetServer, KeyValueStore sourceDb) {
        ConcurrentHashMap<Long, Messaging> messagingHashMap = new ConcurrentHashMap<>();
        return sourceDb.retrieveAllData()
//                .parallel()
                .reduce(new LinkedList<>(), (errors, d) -> {
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
                errors.add(e);
                return errors;
            }
            return errors;
        }, (l0, l1) -> {LinkedList<Exception> l = new LinkedList(l0); l.addAll(l1); return l;});
    }
}
