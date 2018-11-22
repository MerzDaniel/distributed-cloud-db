package server.handler;

import lib.communication.Connection;
import lib.message.*;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.Arrays;

public final class AdminMessageHandler {
    static Logger logger = LogManager.getLogger(AdminMessageHandler.class);

    public static KVAdminMessage handleKvAdminMessage(KVAdminMessage message, ServerState state) {
        logger.info(String.format(
                "Got a message: %s with meta '%s' and serverData '%s'",
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
                return new KVAdminMessage(KVAdminMessage.StatusType.CONFIGURE_SUCCESS);
            case START:
                if (state.runningState != RunningState.IDLE)
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
                return moveData(state, message.serverData);
        }

        throw new NotImplementedException();
    }

    private static KVAdminMessage moveData(ServerState state, ServerData serverData) {
        Connection con;
        try {
            con = new Connection();
            con.connect(serverData.getHost(), serverData.getPort());
            con.readMessage();
        } catch (IOException e) {
            return new KVAdminMessage(KVAdminMessage.StatusType.MOVE_ERROR);
        }

        long errors = state.db.retrieveAllData().parallel().map(
                d -> {
                    KVMessage msg = new KVMessageImpl(d.getKey(), d.getValue(), KVMessage.StatusType.PUT);
                    try {
                        con.sendMessage(((KVMessageImpl) msg).marshall());
                        String responseString = con.readMessage();
                        KVMessage response = (KVMessage) MessageMarshaller.unmarshall(responseString);
                        if (!(response.getStatus() == KVMessage.StatusType.PUT_SUCCESS)) return false;
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
