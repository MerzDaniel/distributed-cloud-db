package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.TimeWatch;
import lib.message.exception.MarshallingException;
import lib.message.graph.response.ResponseMessageImpl;
import lib.message.kv.KVMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

import static client.ui.Util.writeLine;

/**
 * Issues a Query on the server
 */
public class QueryCommand implements Command {
    private String query;
    private final Logger logger = LogManager.getLogger(QueryCommand.class);

    public QueryCommand(String query) {
        this.query = query;
    }

    @Override
    public void execute(ApplicationState state) {
        ResponseMessageImpl graphMessageResponse = null;
        TimeWatch t = TimeWatch.start();
        try {
            graphMessageResponse = state.kvStore.query();
        } catch (IOException e) {
            logger.error("error", e);
            writeLine(String.format("An Error occurred during the QUERY : %s (%d ms)", e.getMessage(), t.time()));
            return;
        } catch (MarshallingException e) {
            logger.error("Error during unmarshalling.", e);
            writeLine("Response from the server was invalid.");
            return;
        }


        if (graphMessageResponse.errorMsg.equals(KVMessage.StatusType.SERVER_STOPPED.name())) {
            logger.info(KVMessage.StatusType.SERVER_STOPPED + String.format("The server is stopped so cannot perform the request query<%s>", query));
            writeLine("The server is stopped so cannot perform the request");
            return;
        }

        if (graphMessageResponse.errorMsg.equals(KVMessage.StatusType.SERVER_WRITE_LOCK.name())) {
            logger.info(KVMessage.StatusType.SERVER_WRITE_LOCK + String.format("The server is locked for writing so cannot perform the request query<%s>", query));
            writeLine("The server is locked for writing. Please try again later");
            return;
        }

        writeLine(String.format("Query >> : '%s'", query));
        writeLine(String.format("Query Result >>: '%s' (%d ms)", graphMessageResponse.data.serialize(), t.time()));
    }

    @Override
    public String toString() {
        return "QUERY<" + query + ">";
    }
}
