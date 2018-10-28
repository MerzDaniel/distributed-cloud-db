package client.ui;

import client.store.KvStore;

/**
 * Contains the application state.
 */
public class ApplicationState {
    public boolean stopRequested = false;
    public KvStore kvStore;
}
