package server.threads.handler;

import lib.message.AdminMessages.FullReplicationMsg;
import lib.message.KVAdminMessage;
import lib.message.Messaging;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import lib.server.RunningState;
import lib.server.TimedRunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;
import server.kv.DbError;
import server.kv.KeyValueStore;
import server.threads.GossipStatusThread;
import server.threads.util.gossip.RunningStates;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.stream.Collectors;

public final class AdminMessageHandler {
    static Logger logger = LogManager.getLogger(AdminMessageHandler.class);

    public static KVAdminMessage handleKvAdminMessage(KVAdminMessage message, ServerState state) throws DbError {
        logger.info(String.format(
                "Got a message: %s with storeMeta '%s' and serverData '%s'",
                message.status,
                message.meta != null ? message.meta.marshall() : "",
                message.serverData != null ? message.serverData.marshall() : "")
        );

        switch (message.status) {
            case CONFIGURE:
                state.meta = message.meta;
                state.currentServerServerData = message.meta.getKvServerList().get(message.currentServerIndex);

                if (state.runningState == RunningState.UNCONFIGURED)
                    state.runningState = RunningState.IDLE;

                try {
                    state.db.init(state.currentServerServerData.getName());

                    state.init(state.currentServerServerData);
                } catch (IOException e) {
                    logger.error("error occurred during initializing the db");
                    throw new DbError(e);
                }

                GossipStatusThread gst = new GossipStatusThread(state);
                gst.start();
                state.serverThreads.add(gst);

                return new KVAdminMessage(KVAdminMessage.StatusType.CONFIGURE_SUCCESS);
            case START:
                if (state.runningState == RunningState.UNCONFIGURED)
                    return new KVAdminMessage(KVAdminMessage.StatusType.START_ERROR);
                state.runningState = RunningState.RUNNING;
                return new KVAdminMessage(KVAdminMessage.StatusType.START_SUCCESS);
            case STOP:
                if (!Arrays.asList(RunningState.RUNNING, RunningState.READONLY).contains(state.runningState))
                    return new KVAdminMessage(KVAdminMessage.StatusType.START_ERROR);
                state.runningState = RunningState.IDLE;
                return new KVAdminMessage(KVAdminMessage.StatusType.STOP_SUCCESS);
            case SHUT_DOWN:
                state.runningState = RunningState.SHUTTINGDOWN;
                return new KVAdminMessage(KVAdminMessage.StatusType.SHUT_DOWN_SUCCESS);
            case STATUS:
                return new KVAdminMessage(KVAdminMessage.StatusType.STATUS_RESPONSE, state.runningState);
            case MOVE:
                state.runningState = RunningState.READONLY;
                return moveData(state, message.serverData, false);
            case MOVE_SOFT:
                state.runningState = RunningState.READONLY;
                return moveData(state, message.serverData, true);
            case DATA_MOVE:
                state.db.put(message.key, message.value);
                return new KVAdminMessage(KVAdminMessage.StatusType.DATA_MOVE_SUCCESS);
            case MAKE_READONLY:
                state.runningState = RunningState.READONLY;
                return new KVAdminMessage(KVAdminMessage.StatusType.MAKE_SUCCESS);
            case GOSSIP_STATUS:
                RunningStates.combineMapsInto(state.stateOfAllServers, message.timedServerStates, state.stateOfAllServers);
                state.stateOfAllServers.put(state.currentServerServerData.getName(), new TimedRunningState(state.runningState));
                return new KVAdminMessage(KVAdminMessage.StatusType.GOSSIP_STATUS_SUCCESS, state.stateOfAllServers);
            case PUT_REPLICATE:
                try {
                    KeyValueStore db = state.dbProvider.getDb(state.meta.findKVServerForKey(message.key));
                    db.put(message.key, message.value);
                } catch (KVServerNotFoundException e) {
                    return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_ERROR);
                }
                return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_SUCCESS);
            case DELETE_REPLICATE:
                try {
                    KeyValueStore db = state.dbProvider.getDb(state.meta.findKVServerForKey(message.key));
                    db.deleteKey(message.key);
                } catch (KVServerNotFoundException e) {
                    return new KVAdminMessage(KVAdminMessage.StatusType.DELETE_REPLICATE_ERROR);
                }
                return new KVAdminMessage(KVAdminMessage.StatusType.DELETE_REPLICATE_SUCCESS);
            case FULL_REPLICATE:
                return doFullReplication((FullReplicationMsg) message, state);
        }

        throw new NotImplementedException();
    }

    private static KVAdminMessage doFullReplication(FullReplicationMsg message, ServerState state) {
        HashMap<Long, Messaging> messagingHashMap = new HashMap<>();
        ServerData targetServer;
        try {
            targetServer = state.meta.findKvServerByName(message.targetServerName);
        } catch (KVServerNotFoundException e) {
            logger.warn("Tried to replicate to non existing server");
            return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_ERROR);
        }

        IntSummaryStatistics errorCount = state.db.retrieveAllData().parallel().map((d) -> {
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
                logger.warn("Problem during replication", e);
                return 1;
            }
            return 0;
        }).collect(Collectors.summarizingInt(x -> (int) x));

        if (errorCount.getSum() > 0)
            return new KVAdminMessage(KVAdminMessage.StatusType.FULL_REPLICATE_ERROR);

        return new KVAdminMessage(KVAdminMessage.StatusType.FULL_REPLICATE_SUCCESS);
    }

    private static KVAdminMessage moveData(ServerState state, ServerData serverData, boolean softMove) {
        Messaging con;
        try {
            con = new Messaging();
            con.connect(serverData.getHost(), serverData.getPort());
        } catch (IOException e) {
            return new KVAdminMessage(KVAdminMessage.StatusType.MOVE_ERROR);
        }

        long errors = state.db.retrieveAllData().parallel().map(
                d -> {
                    try {
                        if (softMove && state.meta.findKVServerForKey(d.getKey()) == state.currentServerServerData) {
                            // for soft move only move data that this server is not responsible for
                            return true;
                        }
                    } catch (KVServerNotFoundException e) {
                        e.printStackTrace();
                    }

                    KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.DATA_MOVE, d.getKey(), d.getValue());
                    try {
                        con.sendMessage(msg);
                        KVAdminMessage response = (KVAdminMessage) con.readMessage();
                        if (!(response.status == KVAdminMessage.StatusType.DATA_MOVE_SUCCESS)) return false;
                    } catch (Exception e) {
                        logger.warn("Error while moving data!", e);
                        return false;
                    }
                    return true;
                }
        ).filter(x -> !x).count();
        if (errors > 0) {
            logger.warn("Got " + errors + "errors while moving data!!!");
            return new KVAdminMessage(KVAdminMessage.StatusType.MOVE_ERROR);
        }

        return new KVAdminMessage(KVAdminMessage.StatusType.MOVE_SUCCESS);
    }
}
