package client.ui;

import client.store.KVStore;
import lib.metadata.KVStoreMetaData;

/**
 * Contains the application state.
 */
public class ApplicationState {
    public boolean stopRequested = false;
    public KVStore kvStore;
    public KVStoreMetaData kvStoreMetaData;
}
