package server;

import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import lib.server.TimedRunningStateMap;
import server.kv.KeyValueStore;

import java.util.Dictionary;

public class ServerState {
    public KVStoreMetaData meta = new KVStoreMetaData();
    public KeyValueStore db;
    public ServerData currentServerServerData;
    public RunningState runningState = RunningState.UNCONFIGURED;
    public TimedRunningStateMap stateOfAllServers = new TimedRunningStateMap();

    public ServerState(KeyValueStore db, ServerData serverData) {
        this.db = db;
        this.currentServerServerData = serverData;
    }

}
