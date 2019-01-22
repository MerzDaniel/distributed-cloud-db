package server.threads.handler;

import lib.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.kv.KVMessage;
import lib.message.kv.MessageFactory;
import org.junit.Before;
import org.junit.Test;
import server.ServerState;
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
        Json doc = Json.Factory.create().withStringProperty(propKey, propVal).finish();

        KVMessage putMsg = MessageFactory.createPutMessage(docId, doc.serialize());
        new PutHandler().handleRequest(putMsg, state);
    }

    @Test
    public void writeNewProp() throws MarshallingException {
        GraphDbMessage mutationMsg = MutationMessageImpl.Factory.create(docId).withReplace(
                newPropKey,
                new Json.StringValue(newPropVal)).finish();
        GraphMessageHandler.handle(mutationMsg,state);

        KVMessage response = new GetHandler().handleRequest(MessageFactory.createGetMessage(docId), state);
        Json newDoc = Json.deserialize(response.getValue());

        assertEquals(newPropVal, newDoc.get(newPropKey).serialize());

    }
}
