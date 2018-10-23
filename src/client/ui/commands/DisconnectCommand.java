package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;

import static client.ui.Util.writeLine;

public class DisconnectCommand implements Command {

    private final boolean outputMessages;

    public DisconnectCommand(boolean outputMessages) {
        this.outputMessages = outputMessages;
    }

    @Override
    public void execute(ApplicationState state) {
        if (!state.connection.isConnected()) {
            if (this.outputMessages)
                writeLine("Currently not connected to a server");
            return;
        }
        state.connection.disconnect();
        if (this.outputMessages)
            writeLine("Disconnected");
    }
}
