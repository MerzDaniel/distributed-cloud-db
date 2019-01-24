package server.threads.handler;

import lib.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.kv.KVMessage;
import lib.message.kv.KvMessageFactory;
import org.junit.Before;
import org.junit.Test;
import server.ServerState;
import server.threads.handler.graph.GraphMessageHandler;
import server.threads.handler.kv.GetHandler;
import server.threads.handler.kv.PutHandler;
import util.TestServerState;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class GraphHandlerTest {
    ServerState state;

    final String docId = "docId";
    final String propKey = "propKey";
    final String propVal = "propVal";
    final String newPropKey = "newPropKey";
    final String newPropVal = "newPropVal";


    @Before
    public void setup() {
        state = TestServerState.create();
        Json doc = Json.Builder.create().withStringProperty(propKey, propVal).finish();

        KVMessage putMsg = KvMessageFactory.createPutMessage(docId, doc.serialize());
        new PutHandler().handleRequest(putMsg, state);
    }

    @Test
    public void writeNewProp() throws MarshallingException {
        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docId).withReplace(
                newPropKey,
                new Json.StringValue(newPropVal)).finish();
        GraphMessageHandler.handle(mutationMsg,state);

        KVMessage response = new GetHandler().handleRequest(KvMessageFactory.createGetMessage(docId), state);
        Json newDoc = Json.deserialize(response.getValue());

        assertEquals(newPropVal, newDoc.get(newPropKey).serialize());

    }

    @Test
    public void replaceExistingProp() throws MarshallingException {
        GraphDbMessage mutationMsg = MutationMessageImpl.Builder.create(docId).withReplace(
                propKey,
                new Json.StringValue(newPropVal)).finish();
        GraphMessageHandler.handle(mutationMsg,state);

        KVMessage response = new GetHandler().handleRequest(KvMessageFactory.createGetMessage(docId), state);
        Json newDoc = Json.deserialize(response.getValue());

        assertEquals(newPropVal, newDoc.get(propKey).serialize());
        assertTrue(newDoc.properties.size() == 1);

    }
    // TODO GRAPH: write a non existing doc (it should be created)
    @Test
    public void writeToNonExistingDoc() throws MarshallingException {
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

    // TODO GRAPH: Write tests for MERGE properties (merge json and arrays)

    //TODO GRAPH: if the document is not a valid json throw an error
}
