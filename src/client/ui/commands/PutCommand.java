package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.message.KVMessage;
import lib.message.KVMessageImpl;
import lib.message.KVMessageMarshaller;
import lib.message.KVMessageUnmarshaller;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static client.ui.Util.writeLine;

public class PutCommand implements Command {
    private String key;
    private String value;

    private final Logger logger = LogManager.getLogger(PutCommand.class);

    public PutCommand(String key, String value){
        this.key = key;
        this.value = value;
    }

    @Override
    public void execute(ApplicationState state) {
        if (!state.connection.isConnected()) {
            writeLine("Currently not connected to a server");
            return;
        }
        KVMessage kvMessageRequest = new KVMessageImpl(key, value, KVMessage.StatusType.PUT);
        state.connection.sendMessage(KVMessageMarshaller.getInstance().marshall(kvMessageRequest));
        String receivedMessage = state.connection.readMessage();
        KVMessage kVMessageResponse = KVMessageUnmarshaller.getInstance().unmarshall(receivedMessage);

        if (kVMessageResponse.isError()) {
            writeLine("An error occurred while executing the command PUT");
            logger.error("An error occurred while executing the command GET, error=" + kVMessageResponse.getStatus());
        }
        if (kVMessageResponse.isSuccess()) {
            writeLine(kVMessageResponse.getStatus().name());
        }
    }
}
