package server;

import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import lib.server.TimedRunningStateMap;
import server.kv.DbProvider;
import server.kv.KeyValueStore;
import server.kv.RandomAccessKeyValueStore;
import server.threads.AbstractServerThread;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServerState {
    public KVStoreMetaData meta = new KVStoreMetaData();
    public DbProvider dbProvider;
    private Map<String, KeyValueStore> replicaMap;
    public ServerData currentServerServerData;
    public RunningState runningState = RunningState.UNCONFIGURED;
    public TimedRunningStateMap stateOfAllServers = new TimedRunningStateMap();
    public List<AbstractServerThread> serverThreads = new LinkedList<>();

    /**
     * Only for testing purposes
     */
    public ServerState(KeyValueStore db, ServerData serverData) {
        this.dbProvider = new DbProvider(serverData, db);
        this.replicaMap = new HashMap<>();
        this.currentServerServerData = serverData;
    }

    /**
     * Should be called for normal startup
     */
    public ServerState(ServerData serverData) {
        this.currentServerServerData = serverData;
    }

    public KeyValueStore getReplica(String serverName) throws IOException {
        if (!replicaMap.containsKey(serverName)) {
            KeyValueStore kvStore = new RandomAccessKeyValueStore();
            kvStore.init("replica<" + serverName + ">_" + currentServerServerData.getName());
            replicaMap.put(serverName, kvStore);
        }

        return replicaMap.get(serverName);
    }

    public void init(ServerData serverData) {
        dbProvider = new DbProvider(serverData);
    }
}
