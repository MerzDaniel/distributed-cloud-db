package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.message.KVMessage;
import lib.message.KVMessageUnmarshaller;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static client.ui.Util.writeLine;

public class GetCommand implements Command {
    private String key;
    private String statusType;
    private final Logger logger = LogManager.getLogger(GetCommand.class);

    public GetCommand(String statusType, String key){
        this.statusType = statusType;
        this.key = key;
    }

    @Override
    public void execute(ApplicationState state) {
        if (!state.connection.isConnected()) {
            writeLine("Currently not connected to a server");
            return;
        }

        state.connection.sendMessage(statusType + "<" + key + ">");
        String receivedMessage = state.connection.readMessage();
        KVMessage kVMessageResponse = new KVMessageUnmarshaller().unmarshall(receivedMessage);

        if (kVMessageResponse.isError()) {
            writeLine("An error occurred while executing the GET");
            logger.error("An error occurred while executing the GET, error=" + kVMessageResponse.getStatus());
        }
        if (kVMessageResponse.isSuccess()) {
            writeLine(kVMessageResponse.getValue());
        }
    }
}
