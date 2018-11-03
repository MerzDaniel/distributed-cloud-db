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

        boolean success = true;
        KVMessage kVMessageResponse = null;
        TimeWatch t = TimeWatch.start();
        try {
            kVMessageResponse = state.kvStore.get(key);
        } catch (IOException e) {
            logger.warn("error", e);
            success = false;
        } catch (UnmarshallException e) {
            writeLine("Response from the server was invalid.");
            return;
        }

        if (!success || kVMessageResponse.isError()) {
            if (kVMessageResponse.getStatus() == KVMessage.StatusType.GET_NOT_FOUND) {
                writeLine(String.format("The key<%s> not found in the database", key));
                logger.error(kVMessageResponse.getStatus() + String.format(": The requested key<%s> is not found in the database", key));
            }
            else {
                writeLine(String.format("An error occurred while executing the GET (%d ms)",t.time()));
                logger.error("An error occurred while executing the GET, error=" + kVMessageResponse.getStatus());
                return;
            }

            return;
        }

        writeLine(String.format("Value of %s is: '%s' (%d ms)", key, kVMessageResponse.getValue(), t.time()));
    }
}
