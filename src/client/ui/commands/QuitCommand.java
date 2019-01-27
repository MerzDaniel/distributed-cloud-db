package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;

/**
 * Exits the application
 */
public class QuitCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        new DisconnectCommand().execute(state);
        state.stopRequested = true;
        System.out.println("Exit application...");
    }
}
