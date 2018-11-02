package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.TimeWatch;
import lib.message.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

import static client.ui.Util.writeLine;

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
        boolean success = true;
        KVMessage kVMessageResponse = null;
        TimeWatch t = TimeWatch.start();
        try {
            kVMessageResponse = state.kvStore.put(key, value);
        } catch (IOException e) {
            logger.warn("error while in put");
            success = false;
        }

        if (!success || kVMessageResponse.isError()) {
            writeLine(String.format("An error occurred while executing the command PUT (%d ms)", t.time()));
            logger.error("An error occurred while executing the command GET, error=" + kVMessageResponse.getStatus());
            return;
        }

        if (value.equals(""))
            writeLine(String.format("Succesfully deleted the entry with key <%s> in the database (%d ms)", key, t.time()));
        else
            writeLine(String.format("Succesfully saved <%s,%s> in the database (%d ms)", key, value, t.time()));
    }
}
