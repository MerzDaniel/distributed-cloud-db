package ui;

import communication.Connection;

/**
 * Contains the application state.
 */
public class ApplicationState {
    public boolean stopRequested = false;
    public Connection connection = new Connection();
}
