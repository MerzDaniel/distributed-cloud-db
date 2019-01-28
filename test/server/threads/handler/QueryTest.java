package server.threads.handler;

import lib.json.Json;
import lib.message.exception.MarshallingException;
import lib.message.exception.UnsupportedJsonStructureFoundException;
import lib.message.graph.query.QueryMessageImpl;
import lib.message.graph.response.ResponseMessageImpl;
import lib.metadata.KVServerNotFoundException;
import org.junit.Before;
import org.junit.Test;
import server.ServerState;
import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.service.Document;
import server.threads.handler.graph.GraphMessageHandler;
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

    final String referencedDocId2 = "refDocId2";
    final String referenced2PropVal = "ref2PropVal";

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
    public void setup() throws KVServerNotFoundException, DbError, MarshallingException, IOException {
        state = TestServerState.create();
        Json doc = Json.Builder.create()
                .withStringProperty(stringPropKey, stringPropVal)
                .withJsonProperty(jsonPropKey, jsonPropVal)
                .withArrayProperty(arrPropKey, arrPropVal)
                .withStringProperty(refPropKey, referencedDocId)
                .withStringArrayProperty(refArrPropKey, Arrays.asList(referencedDocId, referencedDocId2))
                .finish();

        Json referencedDoc = Json.Builder.create()
                .withStringProperty(referencedPropKey, referencedPropVal)
                .finish();

        Json referencedDoc2 = Json.Builder.create()
                .withStringProperty(referencedPropKey, referenced2PropVal)
                .finish();

        Document.writeJsonDocument(docId, doc, state);
        Document.writeJsonDocument(referencedDocId, referencedDoc, state);
        Document.writeJsonDocument(referencedDocId2, referencedDoc2, state);
    }

    @Test
    public void queryStringProp() throws MarshallingException, IOException, DbError, KVServerNotFoundException, KeyNotFoundException, UnsupportedJsonStructureFoundException {
        QueryMessageImpl queryMessage = QueryMessageImpl.Builder.create(docId).withProperty(stringPropKey).finish();
        ResponseMessageImpl response = (ResponseMessageImpl) GraphMessageHandler.handle(queryMessage, state);

        assertEquals(e(stringPropVal), response.data.get(stringPropKey).serialize());
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
        assertEquals(e(stringPropVal), response.data.get(stringPropKey).serialize());
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
        assertEquals(e(referencedPropVal), val.value.get(referencedPropKey).serialize());
    }

    @Test
    public void queryListReferences() throws IOException, DbError, KVServerNotFoundException, MarshallingException, KeyNotFoundException {
        QueryMessageImpl queryMessage = QueryMessageImpl.Builder.create(docId)
                .withFollowReferenceProperty(
                        refArrPropKey,
                        Json.Builder.create()
                                .withUndefinedProperty(referencedPropKey)
                                .finish())
                .finish();

        ResponseMessageImpl response = (ResponseMessageImpl) GraphMessageHandler.handle(queryMessage, state);

        Json.ArrayValue val = (Json.ArrayValue) response.data.get(refArrPropKey);

        assertTrue(val != null);
        assertEquals(2, val.values.size());
        Json result1 = ((Json.JsonValue)val.values.get(0)).value;
        Json result2 = ((Json.JsonValue)val.values.get(1)).value;
        assertEquals(e(referencedPropVal), result1.get(referencedPropKey).serialize());
        assertEquals(e(referenced2PropVal), result2.get(referencedPropKey).serialize());
    }

    /**
     * Escape string (like in serialized json)
     */
    private String e(String s) {
        return "\"" + s + "\"";
    }
}
