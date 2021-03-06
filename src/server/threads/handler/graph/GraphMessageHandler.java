package server.threads.handler.graph;

import lib.json.Json;
import lib.message.IMessage;
import lib.message.exception.MarshallingException;
import lib.message.exception.UnsupportedJsonStructureFoundException;
import lib.message.graph.GraphDbMessage;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.graph.mutation.Operations;
import lib.message.graph.query.QueryMessageImpl;
import lib.message.graph.query.QueryOperation;
import lib.message.graph.query.QueryType;
import lib.message.graph.response.ResponseMessageImpl;
import lib.metadata.KVServerNotFoundException;
import server.ServerState;
import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.service.Document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.stream.Collectors;

public final class GraphMessageHandler {
    /* TODO GRAPH: For external docs request them not using GetHandler but use GetMessage functionality. Currently
       TODO            the GetHandler would return a NotResponsibleMessage. After this is done this GraphHandler can
       TODO             always return ResponseMessages
    */

    public static ResponseMessageImpl handle(GraphDbMessage message, ServerState state) throws MarshallingException, KeyNotFoundException, IOException, KVServerNotFoundException, DbError {
        switch (message.messageType) {
            case QUERY:
                return handleQuery((QueryMessageImpl) message, state);
            case MUTATION:
                return handleMutation((MutationMessageImpl) message, state);
        }
        return new ResponseMessageImpl("Unsupported MessageType!");
    }

    private static ResponseMessageImpl handleMutation(MutationMessageImpl message, ServerState state) {
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

        StringBuilder errors = new StringBuilder();
        for (Json.Property docMutations : message.mutations.properties) {
            Json mutation = ((Json.JsonValue) docMutations.value).value;
            try {
                handleSingleDocMutation(docMutations.key, mutation, state);
            } catch (Exception err) {
                errors.append(err.getMessage());
            }
        }
        return new ResponseMessageImpl(errors.toString());
    }

    private static IMessage handleSingleDocMutation(String docId, Json mutation, ServerState state) throws MarshallingException, UnsupportedJsonStructureFoundException, KVServerNotFoundException, IOException, DbError {
        Json doc;
        try {
            doc = Document.loadJsonDocument(docId, state);
        } catch (Exception notFoundErr) {
            doc = new Json();
        }

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
                    handleMerge(doc, propertyKey, p.value);
                    break;

                case NESTED:
                    // TODO GRAPH: (optional) Implement
                    throw new MarshallingException("Not implemented");

                default:
                    throw new MarshallingException("Not implemented");
            }
        }

        Document.writeJsonDocument(docId, doc, state);

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
    private static ResponseMessageImpl handleQuery(QueryMessageImpl msg, ServerState state) throws MarshallingException, DbError, IOException, KVServerNotFoundException {
        if (msg.queryType != QueryType.ID) new ResponseMessageImpl("QueryType not supported");

        LinkedList<String> errors = new LinkedList();
        Json doc = null;
        try {
            doc = Document.loadJsonDocument(msg.queryParam, state);
        } catch (KeyNotFoundException e) {
            return new ResponseMessageImpl("NOT FOUND");
        }
        Json.Builder responseBuilder = Json.Builder.create();
        for (Json.Property property : msg.request.properties) {
            String opSplit[] = property.key.split(String.format("[%s]", QueryMessageImpl.OPERATION_SEPARATOR));
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

                    if (docPropVal instanceof Json.JsonValue) {
                        responseBuilder.withStringProperty(propKey, "ERR_JSON_NOT_SUPPORTED");
                        continue;
                    }
                    if (docPropVal instanceof Json.StringValue) {
                        String referencedDocId = ((Json.StringValue) docPropVal).value;
                        Json referencedDoc = null;
                        try {
                            referencedDoc = Document.loadJsonDocument(referencedDocId, state);
                            Json result = loadPropsFromDoc(((Json.JsonValue) queryPropValue).value, referencedDoc);
                            responseBuilder.withJsonProperty(propKey, result);
                        } catch (KeyNotFoundException e) {
                            responseBuilder.withStringProperty(propKey, "ERR_NOT_FOUND");
                        }
                        continue;
                    }
                    if (docPropVal instanceof Json.ArrayValue) {
                        Json.ArrayValue result = new Json.ArrayValue();
                        for (Json.PropertyValue value : ((Json.ArrayValue) docPropVal).values) {
                            if (!(value instanceof Json.StringValue)) {
                                result = null;
                                break;
                            }
                            try {
                                Json referencedDoc = null;
                                referencedDoc = Document.loadJsonDocument(((Json.StringValue) value).value, state);
                                Json nestedDoc = loadPropsFromDoc(((Json.JsonValue) queryPropValue).value, referencedDoc);
                                result.values.add(new Json.JsonValue(loadPropsFromDoc(((Json.JsonValue) queryPropValue).value, nestedDoc)));
                            } catch (KeyNotFoundException e) {
                                result.values.add(new Json.StringValue("ERR_NOT_FOUND_" + ((Json.StringValue) value).value));
                            }
                        }
                        if (result == null)
                            responseBuilder.withStringProperty(propKey, "ERR_NOT_FOUND");
                        else
                            responseBuilder.withProperty(propKey, result);
                        continue;
                    }

                } else {
                    errors.add("Follow prop operation must have a json value");
                    responseBuilder.withStringProperty(propKey, "FOLLOW_PROP_MUST_HAVE_JSON_VALUE");
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

    private static void handleMerge(Json doc, String key, Json.PropertyValue value) throws UnsupportedJsonStructureFoundException {
        Json.PropertyValue docProp = doc.get(key);

        if (docProp == null) {
            doc.set(key, value);
            return;
        }

        if (docProp instanceof Json.JsonValue && value instanceof Json.JsonValue) {
            Json.JsonValue docPropJv = (Json.JsonValue) docProp;
            ((Json.JsonValue) value).value.properties.stream().forEach(it -> docPropJv.value.setProperty(new Json.Property(it.key, it.value)));
            return;
        }

        if (docProp instanceof Json.ArrayValue && value instanceof Json.JsonValue) {
            Json.ArrayValue docPropJv = (Json.ArrayValue) docProp;
            docPropJv.values.add(value);
            return;
        }

        if (docProp instanceof Json.ArrayValue && value instanceof Json.ArrayValue) {
            Json.ArrayValue docPropJv = (Json.ArrayValue) docProp;
            docPropJv.values.addAll((((Json.ArrayValue) value).values));
            return;
        }

        throw new UnsupportedJsonStructureFoundException();
    }

}
