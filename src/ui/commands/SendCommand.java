package ui.commands;

import ui.ApplicationState;
import ui.Command;

import static ui.Util.writeLine;

public class SendCommand implements Command {
    private String message;

    public SendCommand(String message){
        this.message = message;
    }

    @Override
    public void execute(ApplicationState state) {
        if (!state.connection.isConnected()) {
            writeLine("Currently not connected to a server");
            return;
        }
        state.connection.sendMessage(message);
        String receivedMessage = state.connection.readMessage();
        writeLine(receivedMessage);
    }
}
