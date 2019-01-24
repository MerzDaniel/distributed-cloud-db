package server.threads.handler.graph;

import lib.json.Json;
import lib.message.IMessage;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.graph.query.QueryMessageImpl;
import lib.message.graph.response.ResponseMessageImpl;
import lib.message.graph.mutation.Operations;
import lib.message.kv.KVMessage;
import lib.message.kv.KvMessageFactory;
import server.ServerState;
import server.threads.handler.kv.GetHandler;
import server.threads.handler.kv.PutHandler;

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
        /*
        MUTATION <document-id> {
            <property-key-1>|REPLACE: new Value,
            <property-key-2>|MERGE: [ value which will be appended to list ],
            <property-key-2>|NESTED: {
                <nested-property-1>|REPLACE: new value for nested prop
            }
        }

        Example mutation

        old document:
        userDocumentId{
            username: peter,
            messages: [ messageId-1, messageId-2 ]
        }

        MUTATION userDocumentId {
            username|REPLACE: christian,
            messages|MERGE: [ messageId-3 ]
        }

        updated document:
        userDocumentId{
            username: christian,
            messages: [ messageId-1, messageId-2, messageId-3 ]
        }

         */
        IMessage docResponse = new GetHandler().handleRequest(KvMessageFactory.createGetMessage(message.key), state);

        if (!((KVMessage) docResponse).isSuccess()) return docResponse; // GET not successful (e.g. not responsible)

        Json doc = Json.deserialize(((KVMessage) docResponse).getValue());

        for (Json.Property p : message.mutations.properties) {
            // two operations allowed: REPLACE and MERGE (and possibly NESTED)
            String operationSplit[] = p.key.split("[|]");
            String propertyKey = operationSplit[0];
            Operations op = Operations.valueOf(operationSplit[1]);
            switch (op) {
                case REPLACE:
                    doc.setProperty(new Json.Property(propertyKey, p.value));
                    break;

                case MERGE:
                    // TODO GRAPH: Implement
                    throw new MarshallingException("Not implemented");

                case NESTED:
                    // TODO GRAPH: (optional) Implement
                    throw new MarshallingException("Not implemented");

                default:
                    throw new MarshallingException("Not implemented");
            }
        }

        KVMessage putResponse = new PutHandler().handleRequest(KvMessageFactory.createPutMessage(message.key, doc.serialize()), state);

        if (!putResponse.isSuccess()) {
            return new ResponseMessageImpl("Mutation failed: " + putResponse.getStatus());
        }

        // success
        return new ResponseMessageImpl();
    }

    private static ResponseMessageImpl handleQuery(QueryMessageImpl msg) {
        // TODO GRAPH: implement query
        // example layout:
        /*
        QUERY ID {
            <id-of-some-document>: {
                messages|FOLLOW: [
                    { to: [ { name, } ] }
                ],
                <property-key-1>: ,
                <property-key-2>: ,
                <property-key-of-reference-to-other-doc>|FOLLOW: {
                    <property-of-other-doc>
                }
            }
        }

        RESPONSE {
            <id-of-some-document>: {
                <property-key-1>: <value-1-from-database>,
                <property-key-2>: <value-2-from-database>,
                <property-key-of-reference-to-other-doc>: {
                    <property-of-other-doc>: <value-from-other-doc>
                }
            }
        }

        doc1: { key: value, refKey: doc2}
        doc2: { key: value2, key2: value3 }
        query ID { doc1: { key, refKey|FOLLOW: { key } } }
        response { key: value , refKey: { key: value2 }}
         */
        return null;
    }
}
