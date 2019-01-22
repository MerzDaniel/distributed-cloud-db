package server.threads.handler.graph;

import lib.message.graph.GraphDbMessage;
import lib.message.graph.MutationMessageImpl;
import lib.message.graph.QueryMessageImpl;
import lib.message.graph.ResponseMessageImpl;

public class GraphMessageHandler {
    public ResponseMessageImpl handle(GraphDbMessage message) {
        switch (message.messageType) {
            case QUERY: return handleQuery((QueryMessageImpl) message);
            case MUTATION: return handleMutation((MutationMessageImpl) message);


        }
        return new ResponseMessageImpl("");
    }

    private ResponseMessageImpl handleMutation(MutationMessageImpl message) {
        return null;
    }

    private ResponseMessageImpl handleQuery(QueryMessageImpl msg) {
        return null;
    }
}
