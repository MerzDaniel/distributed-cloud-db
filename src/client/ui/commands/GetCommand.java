package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.TimeWatch;
import lib.message.KVMessage;
import lib.message.MarshallingException;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.KVStoreMetaData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static client.ui.Util.writeLine;
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
            writeLine("Key is too long. Only 20characters are allowed.");
            return;
        }

        if (!state.kvStore.isConnected()) {
            writeLine("Currently not connected to a server!");
            return;
        }

        KVMessage kVMessageResponse = null;
        TimeWatch t = TimeWatch.start();
        try {
            kVMessageResponse = state.kvStore.get(key);
        } catch (IOException e) {
            logger.error("error", e);
            writeLine(String.format("Error during GET. Possibly the connection to the db got lost (%d ms)",t.time()));
            return;
        } catch (MarshallingException e) {
            logger.error("Error during unmarshalling.", e);
            writeLine("Response from the server was invalid.");
            return;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error during hashing.", e);
            writeLine("An unexpected error occurred while GET");
            return;
        } catch (KVServerNotFoundException e) {
            logger.error(String.format("Couldn't find the server responsible for the key <%s>", key), e);
            writeLine("Couldn't find the server to connect");
            return;
        }

        if (kVMessageResponse.getStatus() == KVMessage.StatusType.GET_NOT_FOUND) {
            logger.info(kVMessageResponse.getStatus() + String.format(": The requested key<%s> is not found in the database", key));
            writeLine(String.format("The key '%s' was not found in the database", key));
            return;
        }

        if (kVMessageResponse.getStatus() == KVMessage.StatusType.SERVER_NOT_RESPONSIBLE) {
            logger.info(String.format("This server is not responsible for the key %s", kVMessageResponse.toString()));
            try {
                state.kvStore.kvStoreMetaData = KVStoreMetaData.unmarshall(kVMessageResponse.getValue());
                logger.info("The kvstore meta data is updated");

                this.execute(state);
            } catch (MarshallingException e) {
                logger.error("Error occurred during unmarshalling meta data", e);
                writeLine("Unexpected error occured when executing the GET command");
            }
            return;
        }

        if (kVMessageResponse.getStatus() != KVMessage.StatusType.GET_SUCCESS) {
            writeLine("GET was not successful. Possibly connection hang up.");
            logger.warn(String.format("Get '%s' was not successful: '%s'. Possibly an error in the db. ", kVMessageResponse.toString()));
            return;
        }

        writeLine(String.format("Value of %s is: '%s' (%d ms)", key, kVMessageResponse.getValue(), t.time()));
    }
}
