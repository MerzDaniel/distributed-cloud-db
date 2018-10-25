package lib;

import junit.framework.TestCase;
import org.junit.Test;


public class KVMarshallerTest extends TestCase {

    @Test
    public void testMarshall() {
        KVMessage kvMessage = new KVMessageImpl("Name", "TUM", KVMessage.StatusType.PUT);
        KVMessageMarshaller kvMessageMarshaller = new KVMessageMarshaller();
        String unmarshalledMessage = kvMessageMarshaller.marshall(kvMessage);

        assertEquals("PUT<Name,TUM>", unmarshalledMessage);
    }

    @Test
    public void testMarshallSpecialCharacters() {
        KVMessage kvMessage = new KVMessageImpl("<N<ame/,", ",T/UM>", KVMessage.StatusType.PUT);
        KVMessageMarshaller kvMessageMarshaller = new KVMessageMarshaller();
        String unmarshalledMessage = kvMessageMarshaller.marshall(kvMessage);

        assertEquals("PUT</<N/<ame///,,/,T//UM/>>", unmarshalledMessage);
    }

}

