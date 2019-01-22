package server.threads.handler;

import lib.message.kv.KVMessage;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import server.ServerState;

import java.util.List;
import java.util.Optional;

public class MessageHandlerUtils {

    public static boolean isResponsible(ServerState state, String key, KVMessage.StatusType status) {
        switch (status) {
            case GET:
                return isResponsibleCoordinator(state, key) || isResponsibleReplica(state, key);
            case PUT:
                return isResponsibleCoordinator(state, key);
            default:
                return true;
        }
    }

    /**
     * Check the this server is the coordinator server for the provided {@code key}
     *
     * @param state {@link ServerState}
     * @param key String key
     * @return true if this server is the coordinator server, otherwise false
     */
    public static boolean isResponsibleCoordinator(ServerState state, String key) {
        ServerData responsibleServer;
        try {
            responsibleServer = state.meta.findKVServerForKey(key);
            return responsibleServer.getHost().equals(state.currentServerServerData.getHost()) &&
                    responsibleServer.getPort() == state.currentServerServerData.getPort();
        } catch (KVServerNotFoundException e) {
            return false;
        }
    }

    /**
     * Check the this server is a responsible replica server for the provided {@code key}
     *
     * @param state {@link ServerState}
     * @param key String key
     * @return true if this server is a responsible replica server, otherwise false
     */
    public static boolean isResponsibleReplica(ServerState state, String key) {
        List<ServerData> replicaServers;
        try {
            replicaServers = state.meta.findReplicaKVServers(key);
            Optional<ServerData> serverData = replicaServers.stream().filter(it -> it.getHost().equals(state.currentServerServerData.getHost()) &&
                    it.getPort() == state.currentServerServerData.getPort()).findAny();
            return serverData.isPresent();
        } catch (KVServerNotFoundException e) {
            return false;
        }
    }

    /**
     * Get the responsible {@link ServerData} for the {@code key}
     *
     * @param state {@link ServerState}
     * @param key String key
     * @return ServerData which is responsible for the
     */
    public static ServerData getResponsibleServer(ServerState state, String key) throws KVServerNotFoundException {
        return state.meta.findKVServerForKey(key);
    }
}

