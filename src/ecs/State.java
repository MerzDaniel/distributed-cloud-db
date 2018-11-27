package ecs;

import lib.metadata.KVStoreMetaData;

/**
 * This class keeps the state of the {@link server.KVServer}s
 */
public class State {
    public KVStoreMetaData storeMeta = new KVStoreMetaData();
    public KVStoreMetaData poolMeta = new KVStoreMetaData();
}
