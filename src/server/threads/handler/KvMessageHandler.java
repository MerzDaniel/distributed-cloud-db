package server.threads.handler;

import lib.message.KVMessage;
import lib.message.MessageFactory;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static lib.message.MessageUtil.isValidKey;
import static lib.message.MessageUtil.isValidValue;

public final class KvMessageHandler {
    static Logger logger = LogManager.getLogger(KvMessageHandler.class);

    public static KVMessage handleKvMessage(KVMessage kvMessage, ServerState state) {
        if (
                !Arrays.asList(RunningState.RUNNING, RunningState.READONLY).contains(state.runningState)) {
            logger.info(String.format("Client issued %s while server is in state %s",
                    kvMessage.getStatus().toString(), state.runningState.toString())
            );
            return MessageFactory.creatServerStopped();
        }

        if (!isResponsible(state, kvMessage.getKey(), kvMessage.getStatus())) {
            return MessageFactory.createServerNotResponsibleMessage(kvMessage.getKey(), state.meta.marshall());
        }

        if (state.runningState == RunningState.READONLY && kvMessage.getStatus() != KVMessage.StatusType.GET) {
            logger.info(String.format("Client issued %s while server is in state %s",
                    kvMessage.getStatus().toString(), state.runningState.toString())
            );
            return MessageFactory.createServerWriteLock();
        }

        logger.debug(String.format(
                "Got a message: %s <%s,%s>",
                kvMessage.getStatus(), kvMessage.getKey(), kvMessage.getValue()
        ));

        if (!isValidKeyValueLength(kvMessage)) {
            logger.info(String.format("Key or Value are too long. Only a size for key/value of 20/120kb is allowed. key=%S | value=%s", kvMessage.getKey(), kvMessage.getValue()));
            return MessageFactory.createInvalidMessage();
        }

        switch (kvMessage.getStatus()) {
            case GET:
                return new GetHandler().handleRequest(kvMessage, state);
            case PUT:
                logger.debug(String.format("New PUT message from client: <%s,%s>", kvMessage.getKey(), kvMessage.getValue()));
                return new PutHandler().handleRequest(kvMessage, state);
            case DELETE:
                return MessageFactory.createDeleteErrorMessage();
            default:
                return MessageFactory.createInvalidMessage();
        }
    }

    private static boolean isValidKeyValueLength(KVMessage message) {
        switch (message.getStatus()) {
            case GET:
                return isValidKey(message.getKey());
            case PUT:
                return isValidKey(message.getKey()) && isValidValue(message.getValue());
            case DELETE:
                return isValidKey(message.getKey());
        }

        return true;
    }

    private static boolean isResponsible(ServerState state, String key, KVMessage.StatusType status) {
        switch (status) {
            case GET:
                return isResponsibleCoordinator(state, key) || isResponsibleReplica(state, key);
            case PUT:
                return isResponsibleCoordinator(state, key);
            default:
                return true;
        }
    }

    private static boolean isResponsibleCoordinator(ServerState state, String key) {
        ServerData responsibleServer;
        try {
            responsibleServer = state.meta.findKVServer(key);
            return responsibleServer.getHost().equals(state.currentServerServerData.getHost()) &&
                    responsibleServer.getPort() == state.currentServerServerData.getPort();
        } catch (KVServerNotFoundException e) {
            return false;
        }
    }

    private static boolean isResponsibleReplica(ServerState state, String key) {
        List<ServerData> replicaServers;
        try {
            replicaServers = state.meta.findReplicaKVServersOf(key);
            Optional<ServerData> serverData = replicaServers.stream().filter(it -> it.getHost().equals(state.currentServerServerData.getHost()) &&
                    it.getPort() == state.currentServerServerData.getPort()).findAny();
            return serverData.isPresent();
        } catch (KVServerNotFoundException e) {
            return false;
        }
    }
}