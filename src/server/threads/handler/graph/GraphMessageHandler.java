package server.threads.handler.graph;

import lib.Json;
import lib.message.IMessage;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;
import lib.message.graph.MutationMessageImpl;
import lib.message.graph.QueryMessageImpl;
import lib.message.graph.ResponseMessageImpl;
import lib.message.graph.mutation.Operations;
import lib.message.kv.KVMessage;
import lib.message.kv.MessageFactory;
import server.ServerState;
import server.threads.handler.kv.GetHandler;

import static lib.message.graph.mutation.Operations.MERGE;
import static lib.message.graph.mutation.Operations.REPLACE;

public final class GraphMessageHandler {

    public static IMessage handle(GraphDbMessage message, ServerState state) throws MarshallingException {
        switch (message.messageType) {
            case QUERY:
                return handleQuery((QueryMessageImpl) message);
            case MUTATION:
                return handleMutation((MutationMessageImpl) message, state);
        }
        return new ResponseMessageImpl("Unsupported MessageType!");
    }

    private static IMessage handleMutation(MutationMessageImpl message, ServerState state) throws MarshallingException {
        IMessage docResponse = new GetHandler().handleRequest(MessageFactory.createGetMessage(message.key), state);

        if (!((KVMessage) docResponse).isSuccess()) return docResponse;

        Json doc = Json.deserialize(((KVMessage) docResponse).getValue());

        for (Json.Property p : message.mutations.properties) {
            // two operations allowed: REPLACE and MERGE (and possibly NESTED)
            String operationSplit[] = p.key.split("|");
            String propertyKey = operationSplit[0];
            Operations op = Operations.valueOf(operationSplit[1]);
            switch (op) {
                case REPLACE:
                    doc.setProperty(new Json.Property(propertyKey, p.value));
                    break;

                case MERGE:
                    throw new MarshallingException("Not implemented");

                case NESTED:
                    throw new MarshallingException("Not implemented");

                default:
                    throw new MarshallingException("Not implemented");
            }
        }

        // success
        return new ResponseMessageImpl();
    }

    private static ResponseMessageImpl handleQuery(QueryMessageImpl msg) {
        return null;
    }
}
