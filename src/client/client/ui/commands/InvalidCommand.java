package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;

import static client.ui.Util.writeLine;

public class InvalidCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        writeLine("Wrong command.\n");
        new HelpCommand().execute(state);
    }
}
