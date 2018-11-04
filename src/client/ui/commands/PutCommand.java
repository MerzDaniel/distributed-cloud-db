package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.TimeWatch;
import lib.message.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

import static client.ui.Util.writeLine;
import static lib.message.MessageUtil.isValidKey;
import static lib.message.MessageUtil.isValidValue;

public class PutCommand implements Command {
    private String key;
    private String value;

    private final Logger logger = LogManager.getLogger(PutCommand.class);

    public PutCommand(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void execute(ApplicationState state) {
        if (!(isValidKey(key) && isValidValue(value))) {
            writeLine("Key or Value are too long. Only a size for key/value of 20/120kb is allowed.");
            return;
        }

        if (!state.kvStore.isConnected()) {
            writeLine("Currently not connected to a server.");
            return;
        }

        KVMessage kVMessageResponse;
        TimeWatch t = TimeWatch.start();
        try {
            kVMessageResponse = state.kvStore.put(key, value);
        } catch (IOException e) {
            writeLine(String.format("An error occurred while executing the command PUT (%d ms)", t.time()));
            logger.error("An error occurred while executing the command GET, error", e);
            return;
        } catch (UnmarshallException e) {
            logger.warn("Got an invalid message.");
            writeLine("PUT was not successful: Response from server was invalid.");
            return;
        }

        if (kVMessageResponse.getStatus() == KVMessage.StatusType.DELETE_SUCCESS
                && (value == null || value.equals(""))) {
            writeLine(String.format("Succesfully deleted the entry with key <%s> from database (%d ms)", key, t.time()));
            return;
        }

        switch (kVMessageResponse.getStatus()) {
            case PUT_SUCCESS:
                writeLine(String.format("Succesfully saved <%s,%s> in the database (%d ms)", key, value, t.time()));
                break;
            case PUT_UPDATE:
                writeLine(String.format("Succesfully updated <%s,%s> in the database (%d ms)", key, value, t.time()));
                break;
            case PUT_ERROR:
                writeLine("PUT was not successful: The db returned an error.");
                break;
            default:
                writeLine(String.format("Got unexpected response from server: %s", kVMessageResponse.getStatus()));
        }
    }
}
