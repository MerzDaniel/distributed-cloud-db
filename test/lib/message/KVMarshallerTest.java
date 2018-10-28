package lib.message;

import junit.framework.TestCase;
import org.junit.Test;


public class KVMarshallerTest extends TestCase {

    @Test
    public void testMarshall() {
        KVMessage kvMessage = new KVMessageImpl("Name", "TUM", KVMessage.StatusType.PUT);
        String unmarshalledMessage = KVMessageMarshaller.marshall(kvMessage);

        assertEquals("PUT<Name,TUM>", unmarshalledMessage);
    }

    @Test
    public void testMarshallSpecialCharacters() {
        KVMessage kvMessage = new KVMessageImpl("<N<ame/,", ",T/UM>", KVMessage.StatusType.PUT);
        String unmarshalledMessage = KVMessageMarshaller.marshall(kvMessage);

        assertEquals("PUT</<N/<ame///,,/,T//UM/>>", unmarshalledMessage);
    }

}

