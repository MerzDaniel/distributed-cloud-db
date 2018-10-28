package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;

import static client.ui.Util.writeLine;

public class DisconnectCommand implements Command {

    public DisconnectCommand() {
    }

    @Override
    public void execute(ApplicationState state) {
        state.kvStore.disconnect();
    }
}
