package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.TimeWatch;
import lib.message.KVMessage;
import lib.message.UnmarshallException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Time;

import static client.ui.Util.writeLine;

public class GetCommand implements Command {
    private String key;
    private final Logger logger = LogManager.getLogger(GetCommand.class);

    public GetCommand(String key) {
        this.key = key;
    }

    @Override
    public void execute(ApplicationState state) {
        if (!state.kvStore.isConnected()) {
            writeLine("Currently not connected to a server!");
        }

        KVMessage kVMessageResponse = null;
        TimeWatch t = TimeWatch.start();
        try {
            kVMessageResponse = state.kvStore.get(key);
        } catch (IOException e) {
            logger.warn("error", e);
            writeLine(String.format("Error during GET. Possibly the connection to the db got lost (%d ms)",t.time()));
            return;
        } catch (UnmarshallException e) {
            logger.warn("Error during unmarshalling.", e);
            writeLine("Response from the server was invalid.");
            return;
        }

        if (kVMessageResponse.getStatus() == KVMessage.StatusType.GET_NOT_FOUND) {
            logger.info(kVMessageResponse.getStatus() + String.format(": The requested key<%s> is not found in the database", key));
            writeLine(String.format("The key '%s' was not found in the database", key));
            return;
        }

        if (kVMessageResponse.getStatus() != KVMessage.StatusType.GET_SUCCESS) {
            logger.warn(String.format("Get '%s' was not successful: '%s'. Possibly an error in the db. ", kVMessageResponse.toString()));
            writeLine("GET was not successful. Possibly connection hang up.");
            return;
        }

        writeLine(String.format("Value of %s is: '%s' (%d ms)", key, kVMessageResponse.getValue(), t.time()));
    }
}
