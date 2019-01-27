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
import java.security.NoSuchAlgorithmException;

import static lib.message.MessageUtil.isValidKey;
import static lib.message.MessageUtil.isValidValue;

/**
 * Writes values on the server
 */
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
            System.out.println("Key or Value are too long. Only a size for key/value of 20/120kb is allowed.");
            return;
        }

        KVMessage kVMessageResponse;
        TimeWatch t = TimeWatch.start();
        try {
            kVMessageResponse = state.kvStore.put(key, value);
        } catch (IOException e) {
            System.out.println(String.format("An error occurred during the PUT : %s (%d ms)", e.getMessage(), t.time()));
            logger.error("An error occurred while executing the command PUT, error", e);
            return;
        } catch (MarshallingException e) {
            logger.error("Got an invalid message.");
            System.out.println("PUT was not successful: Response from server was invalid.");
            return;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error during hashing.", e);
            System.out.println("An error occurred while executing the command PUT");
            return;
        } catch (KVServerNotFoundException e) {
            logger.error(String.format("Couldn't find the server responsible for the key <%s>", key), e);
            System.out.println("Couldn't find the server to connect");
            return;
        }

        if (kVMessageResponse.getStatus() == KVMessage.StatusType.DELETE_SUCCESS
                && (value == null || value.equals("") || value.equals("null"))) {
            System.out.println(String.format("Succesfully deleted the entry with key <%s> from database (%d ms)", key, t.time()));
            return;
        }

        switch (kVMessageResponse.getStatus()) {
            case PUT_SUCCESS:
                System.out.println(String.format("Succesfully saved <%s,%s> in the database (%d ms)", key, value, t.time()));
                break;
            case PUT_UPDATE:
                System.out.println(String.format("Succesfully updated <%s,%s> in the database (%d ms)", key, value, t.time()));
                break;
            case PUT_ERROR:
                System.out.println("PUT was not successful: The db returned an error.");
                break;
            case SERVER_STOPPED:
                logger.info(kVMessageResponse.getStatus() + String.format("The server is stopped so cannot perform the request key<%s>", key));
                System.out.println("The server is stopped so cannot perform the request");
                break;
            case SERVER_WRITE_LOCK:
                logger.info(kVMessageResponse.getStatus() + String.format("The server is locked for writing so cannot perform the request key<%s>", key));
                System.out.println("The server is locked for writing. Please try again later");
                break;
            default:
                System.out.println(String.format("Got unexpected response from server: %s", kVMessageResponse.getStatus()));
        }
    }

    @Override
    public String toString() {
        return "PUT<" + key + "," + value +">";
    }
}
