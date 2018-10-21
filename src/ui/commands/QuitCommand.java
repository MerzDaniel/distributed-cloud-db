package ui.commands;

import ui.ApplicationState;
import ui.Command;

import static ui.Util.writeLine;

public class QuitCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        new DisconnectCommand(false).execute(state);
        state.stopRequested = true;
        writeLine("Exit application...");
    }
}
