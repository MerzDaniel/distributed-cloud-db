package server.threads.handler;

import lib.message.KVMessage;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import server.ServerState;
import server.kv.KeyValueStore;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static int getReplicaIndex(ServerState state, String key) {
        List<ServerData> replicaServers;
        List<Integer> replicaIndices;
        try {
            replicaServers = state.meta.findReplicaKVServers(key);
            replicaIndices = IntStream.range(0, replicaServers.size())
                    .filter(index -> replicaServers.get(index).getHost().equals(state.currentServerServerData.getHost()) &&
                            replicaServers.get(index).getPort() == state.currentServerServerData.getPort())
                    .mapToObj(i -> i)
                    .collect(Collectors.toList());
        } catch (KVServerNotFoundException e) {
            return 0;
        }

        if (replicaIndices.size() == 0)
            return 0;

        return replicaIndices.get(0) + 1;
    }

    /**
     * Get the correct database for the {@code key}
     * @param state {@Link ServerState}
     * @param key String key
     * @return the {@link KeyValueStore}
     * @throws NoKeyValueStoreException if the {@link KeyValueStore} cannot be found
     */
    public static KeyValueStore getDatabase(ServerState state, String key) throws NoKeyValueStoreException{
        if (isResponsibleCoordinator(state, key))
            return state.db;

        if (isResponsibleReplica(state, key)) {
            int index = getReplicaIndex(state, key);

            switch (index) {
                case 1:
                    return state.dbReplica1;
                case 2:
                    return state.dbReplica2;
                default:
                    throw new NoKeyValueStoreException();

            }
        }
        throw new NoKeyValueStoreException();
    }
}
