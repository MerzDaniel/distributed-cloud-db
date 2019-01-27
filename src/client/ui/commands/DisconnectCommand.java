package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;

/**
 * Disconnects from a server
 */
public class DisconnectCommand implements Command {

    public DisconnectCommand() {
    }

    @Override
    public void execute(ApplicationState state) {
        if (!state.kvStore.isConnected()) {
            System.out.println("Currently not connected to a server");
            return;
        }
        state.kvStore.disconnect();
        System.out.println("Successfully disconnected from the server.");
    }
}
