package server.threads.handler;

import lib.json.Json;
import lib.message.exception.MarshallingException;
import lib.message.exception.UnsupportedJsonStructureFoundException;
import lib.message.graph.query.QueryMessageImpl;
import lib.message.graph.response.ResponseMessageImpl;
import lib.message.kv.KvMessageFactory;
import lib.metadata.KVServerNotFoundException;
import org.junit.Before;
import org.junit.Test;
import server.ServerState;
import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.threads.handler.graph.GraphMessageHandler;
import server.threads.handler.kv.PutHandler;
import util.TestServerState;

import java.io.IOException;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class QueryTest {

    ServerState state;

    final String referencedDocId = "refDocId";
    final String referencedPropKey = "refPropKey";
    final String referencedPropVal = "refPropVal";

    final String docId = "docId";
    final String stringPropKey = "propKey";
    final String stringPropVal = "propVal";
    final String nestedStringPropKey = "propKey";
    final String nestedStringPropVal = "propVal";
    final String jsonPropKey = "jsonPropKey";
    final Json jsonPropVal =
            Json.Builder.create().withStringProperty(nestedStringPropKey, nestedStringPropVal).finish();
    final String arrPropKey = "arrPropKey";
    final String arrNestedVal = "arrNestedVal";
    final Json.StringValue arrPropVal[] =
            (Json.StringValue[]) Arrays.asList(new Json.StringValue(arrNestedVal)).toArray();
    final String refPropKey = "refPropKey";
    final String refArrPropKey = "refArrPropKey";

    @Before
    public void setup() {
        state = TestServerState.create();
        Json doc = Json.Builder.create()
                .withStringProperty(stringPropKey, stringPropVal)
                .withJsonProperty(jsonPropKey, jsonPropVal)
                .withArrayProperty(arrPropKey, arrPropVal)
                .withStringProperty(refPropKey, referencedDocId)
                .withArrayProperty(
                        refArrPropKey,
                        (Json.PropertyValue[]) Arrays.asList(new Json.StringValue(referencedDocId)).toArray()
                )
                .finish();

        Json referencedDoc = Json.Builder.create()
                .withStringProperty(referencedPropKey, referencedPropVal)
                .finish();

        PutHandler putHandler = new PutHandler();
        putHandler.handleRequest(
                KvMessageFactory.createPutMessage(docId, doc.serialize()), state
        );
        putHandler.handleRequest(
                KvMessageFactory.createPutMessage(referencedDocId, referencedDoc.serialize()), state
        );
    }

    @Test
    public void queryStringProp() throws MarshallingException, IOException, DbError, KVServerNotFoundException, KeyNotFoundException, UnsupportedJsonStructureFoundException {
        QueryMessageImpl queryMessage = QueryMessageImpl.Builder.create(docId).withProperty(stringPropKey).finish();
        ResponseMessageImpl response = (ResponseMessageImpl) GraphMessageHandler.handle(queryMessage, state);

        assertEquals(stringPropVal, response.data.get(stringPropKey).serialize());
    }

    @Test
    public void queryMultipleProps() throws MarshallingException, IOException, DbError, KVServerNotFoundException, KeyNotFoundException, UnsupportedJsonStructureFoundException {
        QueryMessageImpl queryMessage = QueryMessageImpl.Builder.create(docId)
                .withProperty(stringPropKey)
                .withProperty(arrPropKey)
                .withProperty(jsonPropKey)
                .finish();
        ResponseMessageImpl response = (ResponseMessageImpl) GraphMessageHandler.handle(queryMessage, state);

        assertEquals(3, response.data.properties.size());
        assertEquals(stringPropVal, response.data.get(stringPropKey).serialize());
    }

    @Test
    public void queryNestedDocuments() throws MarshallingException, IOException, DbError, KVServerNotFoundException, KeyNotFoundException, UnsupportedJsonStructureFoundException {
        QueryMessageImpl queryMessage = QueryMessageImpl.Builder.create(docId)
                .withFollowReferenceProperty(
                        refPropKey,
                        Json.Builder.create().withUndefinedProperty(referencedPropKey)
                                .finish())
                .finish();
        ResponseMessageImpl response = (ResponseMessageImpl) GraphMessageHandler.handle(queryMessage, state);

        Json.JsonValue val = (Json.JsonValue) response.data.get(refPropKey);

        assertTrue(val != null);
        assertEquals(referencedPropVal, val.value.get(referencedPropKey).serialize());
    }
}
