package test;

import junit.framework.TestCase;
import lib.KVMessage;
import lib.KVMessageImpl;
import lib.KVMessageMarshaller;
import org.junit.Test;


public class KVMarshallerTest extends TestCase {

    @Test
    public void testMarshall() {
        KVMessage kvMessage = new KVMessageImpl("Name", "TUM", KVMessage.StatusType.PUT);
        KVMessageMarshaller kvMessageMarshaller = new KVMessageMarshaller();
        String unmarshalledMessage = kvMessageMarshaller.marshall(kvMessage);

        assertEquals(unmarshalledMessage, "PUT<Name,TUM>");
    }

    @Test
    public void testMarshallSpecialCharacters() {
        KVMessage kvMessage = new KVMessageImpl("<N<ame/", ",T/UM>", KVMessage.StatusType.PUT);
        KVMessageMarshaller kvMessageMarshaller = new KVMessageMarshaller();
        String unmarshalledMessage = kvMessageMarshaller.marshall(kvMessage);

        assertEquals(unmarshalledMessage, "PUT</<N/<ame//,/,T//UM/>>");
    }

}

