package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;

import static client.ui.Util.writeLine;

/**
 * Informs the user that the typed command does not exist
 */
public class InvalidCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        writeLine("Wrong command.\n");
        new HelpCommand().execute(state);
    }
}
