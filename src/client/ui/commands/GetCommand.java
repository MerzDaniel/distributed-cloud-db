package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.message.KVMessage;
import lib.message.KVMessageUnmarshaller;
import lib.message.UnmarshallException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static client.ui.Util.writeLine;

public class GetCommand implements Command {
    private String key;
    private final Logger logger = LogManager.getLogger(GetCommand.class);

    public GetCommand(String key){
        this.key = key;
    }

    @Override
    public void execute(ApplicationState state) {
        if (!state.connection.isConnected()) {
            writeLine("Currently not connected to a server");
            return;
        }

        state.connection.sendMessage(KVMessage.StatusType.GET.name() + "<" + key + ">");
        String receivedMessage = state.connection.readMessage();
        KVMessage kVMessageResponse = null;
        try {
            kVMessageResponse = KVMessageUnmarshaller.unmarshall(receivedMessage);
        } catch (UnmarshallException e) {
            logger.warn("Invalid response from the server.");
            writeLine("Response from the server was invalid.");
            return;
        }

        if (kVMessageResponse.isError()) {
            writeLine("An error occurred while executing the GET");
            logger.error("An error occurred while executing the GET, error=" + kVMessageResponse.getStatus());
        }
        if (kVMessageResponse.isSuccess()) {
            writeLine(kVMessageResponse.getValue());
        }
    }
}
