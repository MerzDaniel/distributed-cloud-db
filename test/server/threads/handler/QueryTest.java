package server.threads.handler;

import lib.json.Json;
import lib.message.IMessage;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.graph.query.QueryMessageImpl;
import lib.message.graph.response.ResponseMessageImpl;
import lib.message.kv.KVMessage;
import lib.message.kv.KvMessageFactory;
import org.junit.Before;
import org.junit.Test;
import server.ServerState;
import server.threads.handler.graph.GraphMessageHandler;
import server.threads.handler.kv.GetHandler;
import server.threads.handler.kv.PutHandler;
import util.TestServerState;

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
    public void queryStringProp() throws MarshallingException {
        QueryMessageImpl queryMessage = QueryMessageImpl.Builder.create(docId).withProperty(stringPropKey).finish();
        ResponseMessageImpl response = (ResponseMessageImpl) GraphMessageHandler.handle(queryMessage, state);

        assertEquals(stringPropVal, response.data.get(stringPropKey).serialize());
    }
}