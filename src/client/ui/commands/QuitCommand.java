package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;

import static client.ui.Util.writeLine;

/**
 * Exits the application
 */
public class QuitCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        new DisconnectCommand().execute(state);
        state.stopRequested = true;
        writeLine("Exit application...");
    }
}
