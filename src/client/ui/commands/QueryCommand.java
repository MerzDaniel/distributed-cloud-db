package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.TimeWatch;
import lib.json.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.query.QueryMessageImpl;
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
    private String queryParam;
    private String query;

    private final Logger logger = LogManager.getLogger(QueryCommand.class);

    public QueryCommand(String queryParam, String query) {
        this.queryParam = queryParam;
        this.query = query;
    }

    @Override
    public void execute(ApplicationState state) {

        ResponseMessageImpl graphMessageResponse = null;
        TimeWatch t = TimeWatch.start();
        QueryMessageImpl queryMessage = null;
        try {
            queryMessage = constructQueryMsg();
            writeLine(String.format("Query >> : '%s'", queryMessage.prettyPrint()));
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

        if (graphMessageResponse.success()) {
            writeLine(String.format("Query Result >>: '%s' (%d ms)", graphMessageResponse.data.prettyPrint(), t.time()));
            return;
        }

        if (graphMessageResponse.errorMsg.equals(KVMessage.StatusType.SERVER_STOPPED.name())) {
            logger.info(KVMessage.StatusType.SERVER_STOPPED + String.format("The server is stopped so cannot perform the request query<%s>", queryMessage.prettyPrint()));
            writeLine("The server is stopped so cannot perform the request");
            return;
        }

        if (graphMessageResponse.errorMsg.equals(KVMessage.StatusType.SERVER_WRITE_LOCK.name())) {
            logger.info(KVMessage.StatusType.SERVER_WRITE_LOCK + String.format("The server is locked for writing so cannot perform the request query<%s>", queryMessage.prettyPrint()));
            writeLine("The server is locked for writing. Please try again later");
            return;
        }

        writeLine("Errors occured: " + graphMessageResponse.errorMsg);
    }

    @Override
    public String toString() {
        return "QUERY<" + queryParam + " " + query + ">";
    }

    private QueryMessageImpl constructQueryMsg() {
        QueryMessageImpl.Builder queryMessageBuilder = QueryMessageImpl.Builder.create(queryParam);

        String[] querySplits = query.split(",");
        Json.Builder jsonBuilder = null;
        boolean isFollowQuery = false;
        String followKey = null;

        for (String querySplit : querySplits) {
            if (!querySplit.contains("|") && !isFollowQuery) {
                queryMessageBuilder.withProperty(querySplit);
            }
            if (!querySplit.contains("|") && isFollowQuery) {
                if (querySplit.contains("}")) {
                    jsonBuilder.withUndefinedProperty(querySplit.split("}")[0]);
                    queryMessageBuilder.withFollowReferenceProperty(followKey, jsonBuilder.finish());
                    jsonBuilder = null;
                    isFollowQuery = false;
                    followKey = null;
                } else {
                    jsonBuilder.withUndefinedProperty(querySplit);
                }
            }
            if (querySplit.contains("|")) {
                isFollowQuery = true;
                jsonBuilder = Json.Builder.create();
                jsonBuilder.withUndefinedProperty(querySplit.split("[{]")[1]);
                followKey = querySplit.split("[|]")[0];
            }
        }

        return queryMessageBuilder.finish();
    }
}
