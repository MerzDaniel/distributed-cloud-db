package lib.message;

import junit.framework.TestCase;
import org.junit.Test;


public class KVMarshallerTest extends TestCase {

    @Test
    public void testMarshall() {
        KVMessage kvMessage = new KVMessageImpl("Name", "TUM", KVMessage.StatusType.PUT);
        String unmarshalledMessage = KVMessageMarshaller.marshall(kvMessage);

        final String RECORD_SEPARATOR = "\u001E";
        String expected = "PUT" + RECORD_SEPARATOR + "Name" + RECORD_SEPARATOR + "TUM";
        assertEquals(expected, unmarshalledMessage);
    }

    @Test
    public void testMarshallSpecialCharacters() {
        KVMessage kvMessage = new KVMessageImpl("<N<ame/,", ",T/UM>", KVMessage.StatusType.PUT);
        String unmarshalledMessage = KVMessageMarshaller.marshall(kvMessage);

        final String RECORD_SEPARATOR = "\u001E";
        String expected = "PUT" + RECORD_SEPARATOR + "<N<ame/," + RECORD_SEPARATOR + ",T/UM>";
        assertEquals(expected, unmarshalledMessage);
    }

}

