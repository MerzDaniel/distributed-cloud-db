package client.ui;

import client.store.KVStore;

/**
 * Contains the application state.
 */
public class ApplicationState {
    public boolean stopRequested = false;
    public KVStore kvStore;

    public ApplicationState(KVStore kvStore) {
        this.kvStore = kvStore;
    }
}
