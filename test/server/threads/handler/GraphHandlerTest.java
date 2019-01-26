package server.threads.handler;

import lib.json.Json;
import lib.message.exception.MarshallingException;
import lib.message.exception.UnsupportedJsonStructureFoundException;
import lib.message.graph.GraphDbMessage;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.graph.response.ResponseMessageImpl;
import lib.message.kv.KVMessage;
import lib.message.kv.KvMessageFactory;
import lib.metadata.KVServerNotFoundException;
import org.junit.Before;
import org.junit.Test;
import server.ServerState;
import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.threads.handler.graph.GraphMessageHandler;
import server.threads.handler.kv.GetHandler;
import server.threads.handler.kv.PutHandler;
import util.TestServerState;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class GraphHandlerTest {
    ServerState state;

    final String docId = "docId";
    final String propKey = "propKey";
    final String propVal = "propVal";
    final String newPropKey = "newPropKey";
    final String newPropVal = "newPropVal";

    //json with inner json object
    final String docIdWithInnerJson = "docIdWithInnerJson";
    final String innerJsonPropKey = "innerJsonPropKey";
    final String innerJsonPropVal = "innerJsonPropVal";

    //json with json array
    final String docIdWithArray = "docIdWithArray";
    final String propKeyWithArray = "propKeyWithArray";

    @Before
    public void setup() {
        state = TestServerState.create();
        Json doc1 = Json.Builder.create().withStringProperty(propKey, propVal).finish();

        KVMessage putMsg1 = KvMessageFactory.createPutMessage(docId, doc1.serialize());
        new PutHandler().handleRequest(putMsg1, state);

        //setup json data with inner json
        Json innerJson = Json.Builder.create().withStringProperty(innerJsonPropKey, innerJsonPropVal).finish();
        Json doc2 = Json.Builder.create().withJsonProperty(propKey, innerJson).finish();

        KVMessage putMsg2 = KvMessageFactory.createPutMessage(docIdWithInnerJson, doc2.serialize());
        new PutHandler().handleRequest(putMsg2, state);

        //setup json data with json arrays
        Json doc3 = Json.Builder.create().withArrayProperty(propKey, new Json.PropertyValue[]{new Json.JsonValue(doc1), new Json.JsonValue(innerJson)}).finish();

        KVMessage putMsg3 = KvMessageFactory.createPutMessage(docIdWithArray, doc3.serialize());
        new PutHandler().handleRequest(putMsg3, state);
    }

    @Test
    public void writeNewProp() throws MarshallingException, IOException, DbError, KVServerNotFoundException, KeyNotFoundException, UnsupportedJsonStructureFoundException {
        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docId).withReplace(
                newPropKey,
                new Json.StringValue(newPropVal)).finish();
        GraphMessageHandler.handle(mutationMsg,state);

        KVMessage response = new GetHandler().handleRequest(KvMessageFactory.createGetMessage(docId), state);
        Json newDoc = Json.deserialize(response.getValue());

        assertEquals(newPropVal, newDoc.get(newPropKey).serialize());

    }

    @Test
    public void replaceExistingProp() throws MarshallingException, IOException, DbError, KVServerNotFoundException, KeyNotFoundException, UnsupportedJsonStructureFoundException {
        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docId).withReplace(
                propKey,
                new Json.StringValue(newPropVal)).finish();
        GraphMessageHandler.handle(mutationMsg,state);

        KVMessage response = new GetHandler().handleRequest(KvMessageFactory.createGetMessage(docId), state);
        Json newDoc = Json.deserialize(response.getValue());

        assertEquals(newPropVal, newDoc.get(propKey).serialize());
        assertTrue(newDoc.properties.size() == 1);

    }

    @Test
    public void writeToNonExistingDoc() throws MarshallingException, IOException, DbError, KVServerNotFoundException, KeyNotFoundException, UnsupportedJsonStructureFoundException {
        final String nonExistDocId = "nonExisitingDocId";
        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(nonExistDocId).withReplace(
                propKey,
                new Json.StringValue(propVal)).finish();
        GraphMessageHandler.handle(mutationMsg,state);

        KVMessage response = new GetHandler().handleRequest(KvMessageFactory.createGetMessage(nonExistDocId), state);
        Json newDoc = Json.deserialize(response.getValue());

        assertEquals(propVal, newDoc.get(propKey).serialize());
        assertTrue(newDoc.properties.size() == 1);

    }

    @Test
    public void testMergeJson() throws MarshallingException, IOException, UnsupportedJsonStructureFoundException, DbError, KVServerNotFoundException, KeyNotFoundException {
        Json propVal = Json.Builder.create().withStringProperty("key001", "val001").finish();
        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docIdWithInnerJson).withMerge(
                propKey,
                new Json.JsonValue(propVal)).finish();
        GraphMessageHandler.handle(mutationMsg,state);

        KVMessage response = new GetHandler().handleRequest(KvMessageFactory.createGetMessage(docIdWithInnerJson), state);
        Json newDoc = Json.deserialize(response.getValue());

        assertEquals(String.format("{%s:%s,key001,val001}", innerJsonPropKey, innerJsonPropVal), newDoc.get(propKey).serialize());

    }

    @Test
    public void testMergePropForInvalidDocument() throws MarshallingException, IOException, UnsupportedJsonStructureFoundException, DbError, KVServerNotFoundException, KeyNotFoundException {
        Json propVal = Json.Builder.create().withStringProperty("key001", "val001").finish();

        //the property of the document which is going to be merged is type of StringValue
        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docId).withMerge(
                propKey,
                new Json.JsonValue(propVal)).finish();
        ResponseMessageImpl iMessage = (ResponseMessageImpl) GraphMessageHandler.handle(mutationMsg,state);

        assertEquals("Unsupported structure of the message/document to perform the operation", iMessage.errorMsg);
    }

    @Test
    public void testMergePropForInvalidMessage() throws MarshallingException, IOException, UnsupportedJsonStructureFoundException, DbError, KVServerNotFoundException, KeyNotFoundException {

        //the property of the message which is going to be merged is type of StringValue
        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docIdWithInnerJson).withMerge(
                propKey,
                new Json.StringValue("somevalue")).finish();
        ResponseMessageImpl iMessage = (ResponseMessageImpl) GraphMessageHandler.handle(mutationMsg,state);

        assertEquals("Unsupported structure of the message/document to perform the operation", iMessage.errorMsg);
    }

    @Test
    //the document has an string property and the message is an array
    public void testMergeJsonForInvalidDocument() throws MarshallingException, IOException, UnsupportedJsonStructureFoundException, DbError, KVServerNotFoundException, KeyNotFoundException {
        Json propVal = Json.Builder.create().withStringProperty("key001", "val001").finish();

        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docId).withMerge(
                propKey,
                new Json.JsonValue(propVal)).finish();
        ResponseMessageImpl iMessage = (ResponseMessageImpl) GraphMessageHandler.handle(mutationMsg, state);

        assertEquals("Unsupported structure of the message/document to perform the operation", iMessage.errorMsg);
    }

    @Test
    //the document has an array property and the message is an string value
    public void testMergeJsonForInvalidMessage() throws MarshallingException, IOException, UnsupportedJsonStructureFoundException, DbError, KVServerNotFoundException, KeyNotFoundException {
        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docIdWithArray).withMerge(
                propKey,
                new Json.StringValue("newPropVal")).finish();
        ResponseMessageImpl iMessage = (ResponseMessageImpl) GraphMessageHandler.handle(mutationMsg, state);

        assertEquals("Unsupported structure of the message/document to perform the operation", iMessage.errorMsg);
    }

    @Test
    //the document has an array property and the message is an json object
    public void testMergeMsgJsonWithArray() throws MarshallingException, IOException, UnsupportedJsonStructureFoundException, DbError, KVServerNotFoundException, KeyNotFoundException {
        Json json = Json.Builder.create().withStringProperty("key001", "val001").finish();

        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docIdWithArray).withMerge(
                propKey,
                new Json.JsonValue(json)).finish();
        ResponseMessageImpl iMessage = (ResponseMessageImpl) GraphMessageHandler.handle(mutationMsg, state);

        KVMessage response = new GetHandler().handleRequest(KvMessageFactory.createGetMessage(docIdWithArray), state);
        Json newDoc = Json.deserialize(response.getValue());

        assertEquals(String.format("[%s:%s,%s:%s,key001,val001]", propKey, json, innerJsonPropKey, innerJsonPropVal), newDoc.get(propKey).serialize());
    }

    @Test
    //the document has an array property and the message also an array
    public void testMergeMsgArrayWithArray() throws MarshallingException, IOException, UnsupportedJsonStructureFoundException, DbError, KVServerNotFoundException, KeyNotFoundException {
        Json json1 = Json.Builder.create().withStringProperty("key001", "val001").finish();
        Json json2 = Json.Builder.create().withStringProperty("key002", "val002").finish();

        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docIdWithArray).withMerge(
                propKey,
                new Json.ArrayValue(new Json.PropertyValue[]{
                        new Json.JsonValue(json1),
                        new Json.JsonValue(json2)
                })).finish();
        ResponseMessageImpl iMessage = (ResponseMessageImpl) GraphMessageHandler.handle(mutationMsg, state);

        KVMessage response = new GetHandler().handleRequest(KvMessageFactory.createGetMessage(docIdWithArray), state);
        Json newDoc = Json.deserialize(response.getValue());

        assertEquals(String.format("[%s:%s,%s:%s,key001:val001,key002:val002]", propKey, propVal, innerJsonPropKey, innerJsonPropVal), newDoc.get(propKey).serialize());
    }
}
