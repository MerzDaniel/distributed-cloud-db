package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.TimeWatch;
import lib.message.exception.MarshallingException;
import lib.message.kv.KVMessage;
import lib.metadata.KVServerNotFoundException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

import static lib.message.MessageUtil.isValidKey;

/**
 * Issues a GET on the server
 */
public class GetCommand implements Command {
    private String key;
    private final Logger logger = LogManager.getLogger(GetCommand.class);

    public GetCommand(String key) {
        this.key = key;
    }

    @Override
    public void execute(ApplicationState state) {
        if (!isValidKey(key)) {
            System.out.println("Key is too long. Only 20characters are allowed.");
            return;
        }

        KVMessage kVMessageResponse = null;
        TimeWatch t = TimeWatch.start();
        try {
            kVMessageResponse = state.kvStore.get(key);
        } catch (IOException e) {
            logger.error("error", e);
            System.out.println(String.format("An Error occurred during the GET : %s (%d ms)", e.getMessage(), t.time()));
            return;
        } catch (MarshallingException e) {
            logger.error("Error during unmarshalling.", e);
            System.out.println("Response from the server was invalid.");
            return;
        } catch (KVServerNotFoundException e) {
            logger.error(String.format("Couldn't find the server responsible for the key <%s>", key), e);
            System.out.println("Couldn't find the server to connect");
            return;
        }

        if (kVMessageResponse.getStatus() == KVMessage.StatusType.GET_NOT_FOUND) {
            logger.info(kVMessageResponse.getStatus() + String.format(": The requested key<%s> is not found in the database", key));
            System.out.println(String.format("The key '%s' was not found in the database (%dms)", key, t.time()));
            return;
        }

        if (kVMessageResponse.getStatus() == KVMessage.StatusType.SERVER_STOPPED) {
            logger.info(kVMessageResponse.getStatus() + String.format("The server is stopped so cannot perform the request key<%s>", key));
            System.out.println("The server is stopped so cannot perform the request");
            return;
        }

        if (kVMessageResponse.getStatus() == KVMessage.StatusType.SERVER_WRITE_LOCK) {
            logger.info(kVMessageResponse.getStatus() + String.format("The server is locked for writing so cannot perform the request key<%s>", key));
            System.out.println("The server is locked for writing. Please try again later");
            return;
        }

        if (kVMessageResponse.getStatus() != KVMessage.StatusType.GET_SUCCESS) {
            System.out.println("GET was not successful. Possibly connection hang up.");
            logger.warn(String.format("Get '%s' was not successful: '%s'. Possibly an error in the db. ", kVMessageResponse.toString()));
            return;
        }

        System.out.println(String.format("Value of %s is: '%s' (%d ms)", key, kVMessageResponse.getValue(), t.time()));
    }

    @Override
    public String toString() {
        return "GET<" + key + ">";
    }
}
