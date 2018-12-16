package server;

import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import lib.server.TimedRunningStateMap;
import server.kv.DbProvider;
import server.kv.KeyValueStore;
import server.threads.AbstractServerThread;

import java.util.LinkedList;
import java.util.List;

public class ServerState {
    public KVStoreMetaData meta = new KVStoreMetaData();
    public DbProvider dbProvider;
    public ServerData currentServerServerData;
    public RunningState runningState = RunningState.UNCONFIGURED;
    public TimedRunningStateMap stateOfAllServers = new TimedRunningStateMap();
    public List<AbstractServerThread> serverThreads = new LinkedList<>();

    /**
     * Only for testing purposes
     */
    public ServerState(KeyValueStore db, ServerData serverData) {
        this.dbProvider = new DbProvider(serverData, db);
        this.currentServerServerData = serverData;
    }

    /**
     * Should be called for normal startup
     */
    public ServerState(ServerData serverData) {
        this.currentServerServerData = serverData;
    }

    public void init(ServerData serverData) {
        dbProvider = new DbProvider(serverData);
    }
}
