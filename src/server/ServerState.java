package server;

import lib.metadata.KVStoreMetaData;
import server.kv.KeyValueStore;

public class ServerState {
    public KeyValueStore db;

    public ServerState(KeyValueStore db) {
        this.db = db;
    }
}
