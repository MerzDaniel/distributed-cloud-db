package server.threads.handler.admin;

import lib.message.admin.FullReplicationMsg;
import lib.message.admin.ReplicateMsg;
import lib.message.admin.KVAdminMessage;
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
import java.util.Comparator;

public final class AdminMessageHandler {
    static Logger logger = LogManager.getLogger(AdminMessageHandler.class);

    public static KVAdminMessage handleKvAdminMessage(KVAdminMessage message, ServerState state) throws DbError {
        logger.debug(String.format(
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

                state.meta.getKvServerList().sort(Comparator.comparing(ServerData::getFromHash));
                state.init(state.currentServerServerData);

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
                state.dbProvider.getDb(state.currentServerServerData.getName()).
                        put(message.key, message.value);
                return new KVAdminMessage(KVAdminMessage.StatusType.DATA_MOVE_SUCCESS);
            case MAKE_READONLY:
                state.runningState = RunningState.READONLY;
                return new KVAdminMessage(KVAdminMessage.StatusType.MAKE_SUCCESS);
            case GOSSIP_STATUS:
                RunningStates.combineMapsInto(state.stateOfAllServers, message.timedServerStates, state.stateOfAllServers);
                state.stateOfAllServers.put(state.currentServerServerData.getName(), new TimedRunningState(state.runningState));
                return new KVAdminMessage(KVAdminMessage.StatusType.GOSSIP_STATUS_SUCCESS, state.stateOfAllServers);
            case PUT_REPLICATE:
                ReplicateMsg replicateMsg = (ReplicateMsg) message;
                KeyValueStore replicateDb = state.dbProvider.getDb(replicateMsg.srcServerName);
                replicateDb.put(replicateMsg.replicateKey, replicateMsg.replicateValue);
                return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_SUCCESS);
            case DELETE_REPLICATE:
                try {
                    KeyValueStore db = state.dbProvider.getDb(state.meta.findKVServerForKey(message.key).getName());
                    db.deleteKey(message.key);
                } catch (KVServerNotFoundException e) {
                    return new KVAdminMessage(KVAdminMessage.StatusType.DELETE_REPLICATE_ERROR);
                }
                return new KVAdminMessage(KVAdminMessage.StatusType.DELETE_REPLICATE_SUCCESS);
            case FULL_REPLICATE:
                return FullReplication.doFullReplication((FullReplicationMsg) message, state);
        }

        throw new NotImplementedException();
    }

    @Deprecated
    private static KVAdminMessage moveData(ServerState state, ServerData serverData, boolean softMove) {
        Messaging con;
        try {
            con = new Messaging();
            con.connect(serverData.getHost(), serverData.getPort());
        } catch (IOException e) {
            return new KVAdminMessage(KVAdminMessage.StatusType.MOVE_ERROR);
        }

        KeyValueStore db = state.dbProvider.getDb(state.currentServerServerData.getName());
        long errors = db.retrieveAllData().parallel().map(
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
