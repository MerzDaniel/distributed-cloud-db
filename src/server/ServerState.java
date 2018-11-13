package server;

import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import server.kv.KeyValueStore;

public class ServerState {
    public KVStoreMetaData meta = new KVStoreMetaData();
    public KeyValueStore db;
    public ServerData currentServerServerData;
    public State runningState = State.UNCONFIGURED;

    public ServerState(KeyValueStore db, ServerData serverData) {
        this.db = db;
        this.currentServerServerData = serverData;
    }

    public enum State {
        /** No meta configured */
        UNCONFIGURED,
        /** Configured but does not handle client requests */
        IDLE,
        /** Handles client READ requests */
        READONLY,
        /** Normally handle all requests */
        RUNNING,
        /** Server is shutting down will not be reachable in a few seconds */
        SHUTTINGDOWN,
    }
}
