package ui.commands;

import ui.ApplicationState;
import ui.Command;

import static ui.Util.writeLine;

public class InvalidCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        writeLine("Wrong command.\n");
        new HelpCommand().execute(state);
    }
}
