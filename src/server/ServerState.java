package server;

import lib.metadata.KVStoreMetaData;
import lib.metadata.MetaContent;
import server.kv.KeyValueStore;

public class ServerState {
    public final KVStoreMetaData meta = new KVStoreMetaData();
    public KeyValueStore db;
    private MetaContent currentServerMetaContent;

    public ServerState(KeyValueStore db, MetaContent metaContent) {
        this.db = db;
        this.currentServerMetaContent = metaContent;
    }
}
