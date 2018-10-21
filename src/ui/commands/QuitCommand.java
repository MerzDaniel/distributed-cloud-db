package ui.commands;

import ui.ApplicationState;
import ui.Command;

public class QuitCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        new DisconnectCommand().execute(state);
        state.stopRequested = true;
    }
}
