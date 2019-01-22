package server.threads.handler.graph;

import lib.message.graph.GraphDbMessage;
import lib.message.graph.MutationMessageImpl;
import lib.message.graph.QueryMessageImpl;
import lib.message.graph.ResponseMessageImpl;

public final class GraphMessageHandler {
    public static ResponseMessageImpl handle(GraphDbMessage message) {
        switch (message.messageType) {
            case QUERY: return handleQuery((QueryMessageImpl) message);
            case MUTATION: return handleMutation((MutationMessageImpl) message);


        }
        return new ResponseMessageImpl("");
    }

    private static ResponseMessageImpl handleMutation(MutationMessageImpl message) {
        return null;
    }

    private static ResponseMessageImpl handleQuery(QueryMessageImpl msg) {
        return null;
    }
}
