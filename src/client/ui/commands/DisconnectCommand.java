package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;

import static client.ui.Util.writeLine;

/**
 * Disconnects from a server
 */
public class DisconnectCommand implements Command {

    public DisconnectCommand() {
    }

    @Override
    public void execute(ApplicationState state) {
        if (!state.kvStore.isConnected()) {
            writeLine("Currently not connected to a server");
            return;
        }
        state.kvStore.disconnect();
        writeLine("Successfully disconnected from the server.");
    }
}
