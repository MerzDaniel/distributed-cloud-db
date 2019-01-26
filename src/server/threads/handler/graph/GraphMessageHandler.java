package server.threads.handler.graph;

import lib.json.Json;
import lib.message.IMessage;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.graph.query.QueryMessageImpl;
import lib.message.graph.query.QueryOperation;
import lib.message.graph.query.QueryType;
import lib.message.graph.response.ResponseMessageImpl;
import lib.message.graph.mutation.Operations;
import lib.message.kv.KVMessage;
import lib.message.kv.KvMessageFactory;
import lib.metadata.KVServerNotFoundException;
import server.ServerState;
import lib.message.exception.UnsupportedJsonStructureFoundException;
import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.service.Document;
import server.threads.handler.kv.GetHandler;
import server.threads.handler.kv.PutHandler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.stream.Collectors;

public final class GraphMessageHandler {
    /* TODO GRAPH: For external docs request them not using GetHandler but use GetMessage functionality. Currently
       TODO            the GetHandler would return a NotResponsibleMessage. After this is done this GraphHandler can
       TODO             always return ResponseMessages
    */

    public static IMessage handle(GraphDbMessage message, ServerState state) throws MarshallingException, KeyNotFoundException, IOException, KVServerNotFoundException, DbError, UnsupportedJsonStructureFoundException {
        switch (message.messageType) {
            case QUERY:
                return handleQuery((QueryMessageImpl) message, state);
            case MUTATION:
                return handleMutation((MutationMessageImpl) message, state);
        }
        return new ResponseMessageImpl("Unsupported MessageType!");
    }

    private static IMessage handleMutation(MutationMessageImpl message, ServerState state) throws MarshallingException, UnsupportedJsonStructureFoundException {
        /*
        MUTATION <document-id> {
            document-id {
                <property-key-1>|REPLACE: new Value,
                <property-key-2>|MERGE: [ value which will be appended to list ],
                <property-key-2>|NESTED: {
                    <nested-property-1>|REPLACE: new value for nested prop
                }
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

        LinkedList<IMessage> responses = new LinkedList<>();
        for (Json.Property docMutations : message.mutations.properties) {
            Json mutation = ((Json.JsonValue)docMutations.value).value;
            responses.add(handleSingleDocMutation(docMutations.key, mutation, state));
        }
        // TODO GRAPH: combine responses
        return responses.get(0);
    }
    private static IMessage handleSingleDocMutation(String docId, Json mutation, ServerState state) throws MarshallingException, UnsupportedJsonStructureFoundException {

        KVMessage docResponse = new GetHandler().handleRequest(KvMessageFactory.createGetMessage(docId), state);

        if (!docResponse.isSuccess() && !docResponse.getStatus().equals(KVMessage.StatusType.GET_NOT_FOUND)) return docResponse; // GET not successful (e.g. not responsible)

        Json doc = docResponse.getValue() != null ? Json.deserialize(docResponse.getValue()) : Json.Builder.create().finish();

        for (Json.Property p : mutation.properties) {
            // two operations allowed: REPLACE and MERGE (and possibly NESTED)
            String operationSplit[] = p.key.split("[|]");
            String propertyKey = operationSplit[0];
            Operations op = Operations.valueOf(operationSplit[1]);
            switch (op) {
                case REPLACE:
                    doc.setProperty(new Json.Property(propertyKey, p.value));
                    break;

                case MERGE:
                    handleMerge(doc, p);
                    break;

                case NESTED:
                    // TODO GRAPH: (optional) Implement
                    throw new MarshallingException("Not implemented");

                default:
                    throw new MarshallingException("Not implemented");
            }
        }

        KVMessage putResponse = new PutHandler().handleRequest(KvMessageFactory.createPutMessage(docId, doc.serialize()), state);

        if (!putResponse.isSuccess()) {
            return new ResponseMessageImpl("Mutation failed: " + putResponse.getStatus());
        }

        // success
        return new ResponseMessageImpl();
    }

    /**
     * // example layout:
     * QUERY ID {
     * <id-of-some-document>: {
     * messages|FOLLOW: [
     * { to: [ { name, } ] }
     * ],
     * <property-key-1>: ,
     * <property-key-2>: ,
     * <property-key-of-reference-to-other-doc>|FOLLOW: {
     * <property-of-other-doc>
     * }
     * }
     * }
     * <p>
     * RESPONSE {
     * <id-of-some-document>: {
     * <property-key-1>: <value-1-from-database>,
     * <property-key-2>: <value-2-from-database>,
     * <property-key-of-reference-to-other-doc>: {
     * <property-of-other-doc>: <value-from-other-doc>
     * }
     * }
     * }
     * <p>
     * doc1: { key: value, refKey: doc2}
     * doc2: { key: value2, key2: value3 }
     * query ID { doc1: { key, refKey|FOLLOW: { key } } }
     * response { key: value , refKey: { key: value2 }}
     */
    private static ResponseMessageImpl handleQuery(QueryMessageImpl msg, ServerState state) throws MarshallingException, DbError, IOException, KVServerNotFoundException, KeyNotFoundException {
        // TODO GRAPH: implement query
        if (msg.queryType != QueryType.ID) new ResponseMessageImpl("QueryType not supported");

        LinkedList<String> errors = new LinkedList();
        Json doc = Json.deserialize(Document.loadDocument(msg.queryParam, state));
        Json.Builder responseBuilder = Json.Builder.create();
        for (Json.Property property : msg.request.properties) {
            String opSplit[] = property.key.split(String.format("[%s]",QueryMessageImpl.OPERATION_SEPARATOR));
            QueryOperation op;
            if (opSplit.length > 1) {
                op = QueryOperation.valueOf(opSplit[1]);
            } else {
                op = QueryOperation.READ;
            }

            String propKey = opSplit[0];
            Json.PropertyValue queryPropValue = property.value;

            if (doc.get(propKey) == null) {
                responseBuilder.withProperty(propKey, Json.UndefinedValue);
                continue;
            }

            Json.PropertyValue docPropVal = doc.get(propKey);
            if (op == QueryOperation.READ) {
                responseBuilder.withProperty(propKey, docPropVal);
                continue;
            }
            if (op == QueryOperation.FOLLOW) {
                if (queryPropValue instanceof Json.JsonValue) {
                    if (!(docPropVal instanceof Json.StringValue)) throw new NotImplementedException();

                    String referencedDocId = ((Json.StringValue) docPropVal).value;
                    Json referencedDoc = Json.deserialize(Document.loadDocument(referencedDocId, state));
                    Json result = loadPropsFromDoc(((Json.JsonValue) queryPropValue).value, referencedDoc);
                    responseBuilder.withJsonProperty(propKey, result);
                } else {
                    errors.add("Follow prop operation must have a json value");
                }
            }
        }

        if (errors.size() > 0) {
            String errorString = errors.stream().collect(Collectors.joining(";"));
            return new ResponseMessageImpl(errorString);
        }
        return new ResponseMessageImpl(responseBuilder.finish());
    }

    private static Json loadPropsFromDoc(Json query, Json doc) {
        Json.Builder resultBuilder = Json.Builder.create();
        for (Json.Property property : query.properties) {
            Json.PropertyValue docPropVal = doc.get(property.key);
            if (docPropVal == null) docPropVal = Json.UndefinedValue;
            resultBuilder.withProperty(property.key, docPropVal);
        }
        return resultBuilder.finish();
    }

    private static void handleMerge(Json doc, Json.Property msgProp) throws UnsupportedJsonStructureFoundException{
        String key = msgProp.key.split("[|]")[0];
        Json.PropertyValue docProp = doc.get(key);
        if (docProp instanceof Json.JsonValue && msgProp.value instanceof Json.JsonValue) {
            Json.JsonValue docPropJv = (Json.JsonValue) docProp;
            ((Json.JsonValue)msgProp.value).value.properties.stream().forEach(it -> docPropJv.value.setProperty(new Json.Property(it.key, it.value)));
            return;
        }

        if (docProp instanceof Json.ArrayValue && msgProp.value instanceof Json.JsonValue) {
            Json.ArrayValue docPropJv = (Json.ArrayValue) docProp;
            docPropJv.values.add(msgProp.value);
            return;
        }

        if (docProp instanceof Json.ArrayValue && msgProp.value instanceof Json.ArrayValue) {
            Json.ArrayValue docPropJv = (Json.ArrayValue) docProp;
            docPropJv.values.addAll((((Json.ArrayValue) msgProp.value).values));
            return;
        }

        throw new UnsupportedJsonStructureFoundException();
    }

}
