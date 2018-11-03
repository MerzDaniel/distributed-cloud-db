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

        if (!Arrays.asList(
                KVMessage.StatusType.PUT_SUCCESS,
                KVMessage.StatusType.PUT_UPDATE,
                KVMessage.StatusType.PUT_ERROR)
                .contains(kVMessageResponse.getStatus())) {
            writeLine("PUT was not successful: Response from server was invalid.");
            return;
        }

        switch (kVMessageResponse.getStatus()) {
            case PUT_SUCCESS:
                if (value.equals(""))
                    writeLine(String.format("Succesfully deleted the entry with key <%s> in the database (%d ms)", key, t.time()));
                else
                    writeLine(String.format("Succesfully saved <%s,%s> in the database (%d ms)", key, value, t.time()));
                break;
            case PUT_UPDATE:
                writeLine(String.format("Succesfully updated <%s,%s> in the database (%d ms)", key, value, t.time()));
                break;
            case PUT_ERROR:
                writeLine("PUT was not successful: The db returned an error.");
                break;
        }
    }
}
