package server;

import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import lib.server.TimedRunningStateMap;
import server.kv.KeyValueStore;
import server.threads.AbstractServerThread;

import java.util.LinkedList;
import java.util.List;

public class ServerState {
    public KVStoreMetaData meta = new KVStoreMetaData();
    public KeyValueStore db;
    public KeyValueStore db_replica_1;
    public KeyValueStore db_replica_2;
    public ServerData currentServerServerData;
    public RunningState runningState = RunningState.UNCONFIGURED;
    public TimedRunningStateMap stateOfAllServers = new TimedRunningStateMap();
    public List<AbstractServerThread> serverThreads = new LinkedList<>();

    public ServerState(KeyValueStore db, ServerData serverData) {
        this.db = db;
        this.currentServerServerData = serverData;
    }
}
