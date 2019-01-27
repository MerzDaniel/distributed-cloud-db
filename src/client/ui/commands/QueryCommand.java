package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.TimeWatch;
import lib.message.exception.MarshallingException;
import lib.message.graph.query.QueryMessageImpl;
import lib.message.graph.response.ResponseMessageImpl;
import lib.message.kv.KVMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static client.ui.Util.writeLine;

/**
 * Issues a Query on the server
 */
public class QueryCommand implements Command {

    private static final String QUERY = "query";
    private static final String QUERY_BY_USER = "queryByUser";
    private static final String QUERY_BY_MESSAGE = "queryByMessage";

    private String queryParam;
    private List<String> queryProps;

    private final Logger logger = LogManager.getLogger(QueryCommand.class);

    public QueryCommand(String queryParam, List<String> queryProps) {
        this.queryParam = queryParam;
        this.queryProps = queryProps;
    }

    @Override
    public void execute(ApplicationState state) {

        if (!Arrays.asList(QUERY, QUERY_BY_MESSAGE, QUERY_BY_USER).contains(queryParam)) {
            logger.error("unsupported query type");
            writeLine("Unrecognized query type : " + queryParam);
            new HelpCommand().execute(state);
            return;
        }

        ResponseMessageImpl graphMessageResponse = null;
        TimeWatch t = TimeWatch.start();
        QueryMessageImpl queryMessage = null;
        try {
            queryMessage = QueryMessageImpl.Builder.create(queryParam).withProperties(queryProps).finish();
            graphMessageResponse = state.kvStore.query(queryMessage);
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
            logger.info(KVMessage.StatusType.SERVER_STOPPED + String.format("The server is stopped so cannot perform the request query<%s>", this.toString()));
            writeLine("The server is stopped so cannot perform the request");
            return;
        }

        if (graphMessageResponse.errorMsg.equals(KVMessage.StatusType.SERVER_WRITE_LOCK.name())) {
            logger.info(KVMessage.StatusType.SERVER_WRITE_LOCK + String.format("The server is locked for writing so cannot perform the request query<%s>", this.toString()));
            writeLine("The server is locked for writing. Please try again later");
            return;
        }

        writeLine(String.format("Query >> : '%s'", queryMessage));
        writeLine(String.format("Query Result >>: '%s' (%d ms)", graphMessageResponse.data.serialize(), t.time()));
    }

    @Override
    public String toString() {
        return "QUERY<" + queryParam + " " + String.join(",", queryProps) + ">";
    }
}
