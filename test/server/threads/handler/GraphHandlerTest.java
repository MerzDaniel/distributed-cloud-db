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
    // TODO GRAPH: Add tests for (1) replaceExisting prop and for (2) write a non existing doc (it should be created)

    // TODO GRAPH: Write tests for MERGE properties (merge json and arrays)
}
