package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.TimeWatch;
import lib.message.KVMessage;
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
        boolean success = true;
        KVMessage kVMessageResponse = null;
        TimeWatch t = TimeWatch.start();
        try {
            kVMessageResponse = state.kvStore.get(key);
        } catch (IOException e) {
            logger.warn("error", e);
            success = false;
        }

        if (!success || kVMessageResponse.isError()) {
            writeLine(String.format("An error occurred while executing the GET (%d ms)",t.time()));
            logger.error("An error occurred while executing the GET, error=" + kVMessageResponse.getStatus());
            return;
        }

        writeLine(String.format("Value of %s is: '%s' (%d ms)", key, kVMessageResponse.getValue(), t.time()));
    }
}
