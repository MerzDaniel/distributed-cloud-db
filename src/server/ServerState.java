package server;

import lib.metadata.KVStoreMetaData;
import lib.metadata.MetaContent;
import server.kv.KeyValueStore;

public class ServerState {
    public KVStoreMetaData meta = new KVStoreMetaData();
    public KeyValueStore db;
    public MetaContent currentServerMetaContent;
    public State runningState = State.UNCONFIGURED;

    public ServerState(KeyValueStore db, MetaContent metaContent) {
        this.db = db;
        this.currentServerMetaContent = metaContent;
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
