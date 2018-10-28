package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.message.KVMessage;
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
        KVMessage kVMessageResponse = state.kvStore.get(key);

        if (kVMessageResponse.isError()) {
            writeLine("An error occurred while executing the GET");
            logger.error("An error occurred while executing the GET, error=" + kVMessageResponse.getStatus());
        }
        if (kVMessageResponse.isSuccess()) {
            writeLine(kVMessageResponse.getValue());
        }
    }
}
