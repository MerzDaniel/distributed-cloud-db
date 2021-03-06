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
            System.out.println(String.format("Query >> : '%s'", queryMessage.prettyPrint()));
            graphMessageResponse = state.kvStore.query(queryMessage);
        } catch (IOException e) {
            logger.error("error", e);
            System.out.println(String.format("An Error occurred during the QUERY : %s (%d ms)", e.getMessage(), t.time()));
            return;
        } catch (MarshallingException e) {
            logger.error("Error during unmarshalling.", e);
            System.out.println("Response from the server was invalid.");
            return;
        }

        if (graphMessageResponse.success()) {
            System.out.println(String.format("Query Result >>: '%s' (%d ms)", graphMessageResponse.data.prettyPrint(), t.time()));
            return;
        }

        if (graphMessageResponse.errorMsg.equals(KVMessage.StatusType.SERVER_STOPPED.name())) {
            logger.info(KVMessage.StatusType.SERVER_STOPPED + String.format("The server is stopped so cannot perform the request query<%s>", queryMessage.prettyPrint()));
            System.out.println("The server is stopped so cannot perform the request");
            return;
        }

        if (graphMessageResponse.errorMsg.equals(KVMessage.StatusType.SERVER_WRITE_LOCK.name())) {
            logger.info(KVMessage.StatusType.SERVER_WRITE_LOCK + String.format("The server is locked for writing so cannot perform the request query<%s>", queryMessage.prettyPrint()));
            System.out.println("The server is locked for writing. Please try again later");
            return;
        }

        System.out.println("Errors occured: " + graphMessageResponse.errorMsg);
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
                continue;
            }

            if (querySplit.contains("|")) {
                isFollowQuery = true;
                jsonBuilder = Json.Builder.create();
                followKey = querySplit.split("[|]")[0];
            }

            if (isFollowQuery) {
                if (querySplit.contains("{") && querySplit.contains("}")) {
                    jsonBuilder.withUndefinedProperty(querySplit.split("\\{")[1].split("}")[0]);
                    queryMessageBuilder.withFollowReferenceProperty(followKey, jsonBuilder.finish());
                    jsonBuilder = null;
                    isFollowQuery = false;
                    followKey = null;
                    continue;
                }

                if (querySplit.contains("{")) {
                    jsonBuilder.withUndefinedProperty(querySplit.split("\\{")[1]);
                    continue;
                }

                if (querySplit.contains("}")) {
                    jsonBuilder.withUndefinedProperty(querySplit.split("}")[0]);
                    queryMessageBuilder.withFollowReferenceProperty(followKey, jsonBuilder.finish());
                    jsonBuilder = null;
                    isFollowQuery = false;
                    followKey = null;
                    continue;
                }

                if (!querySplit.contains("{") && !querySplit.contains("}")) {
                    jsonBuilder.withUndefinedProperty(querySplit);
                    continue;
                }
            }
        }

        return queryMessageBuilder.finish();
    }
}
