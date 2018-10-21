package ui.commands;

import ui.ApplicationState;
import ui.Command;

import static ui.Util.writeLine;

public class DisconnectCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        if (!state.connection.isConnected()) {
            writeLine("Currently not connected to a server");
            return;
        }
        state.connection.disconnect();
        writeLine("Disconnected");
    }
}
