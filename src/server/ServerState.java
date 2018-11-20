package server;

import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import server.kv.KeyValueStore;

public class ServerState {
    public KVStoreMetaData meta = new KVStoreMetaData();
    public KeyValueStore db;
    public ServerData currentServerServerData;
    public RunningState runningState = RunningState.UNCONFIGURED;

    public ServerState(KeyValueStore db, ServerData serverData) {
        this.db = db;
        this.currentServerServerData = serverData;
    }

}
