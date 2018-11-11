package lib.message;

import junit.framework.TestCase;
import org.junit.Test;


public class MessageMarshallerTest extends TestCase {
    final String RECORD_SEPARATOR = "\u001E";

    @Test
    public void testMarshall() {
        KVMessage kvMessage = new KVMessageImpl("Name", "TUM", KVMessage.StatusType.PUT);
        String unmarshalledMessage = MessageMarshaller.marshall(kvMessage);

        String expected = "PUT" + RECORD_SEPARATOR + "Name" + RECORD_SEPARATOR + "TUM";
        assertEquals(expected, unmarshalledMessage);
    }

    @Test
    public void testMarshallForNullPair() {
        KVMessage kvMessage = new KVMessageImpl(null, null, KVMessage.StatusType.PUT);
        String unmarshalledMessage = MessageMarshaller.marshall(kvMessage);

        String expected = "PUT" + RECORD_SEPARATOR + "" + RECORD_SEPARATOR + "";
        assertEquals(expected, unmarshalledMessage);
    }

    @Test
    public void testMarshallForEmptyPair() {
        KVMessage kvMessage = new KVMessageImpl("", "", KVMessage.StatusType.PUT);
        String unmarshalledMessage = MessageMarshaller.marshall(kvMessage);

        String expected = "PUT" + RECORD_SEPARATOR + "" + RECORD_SEPARATOR + "";
        assertEquals(expected, unmarshalledMessage);
    }

    @Test
    public void testMarshallSpecialCharacters() {
        KVMessage kvMessage = new KVMessageImpl("<N<ame/,", ",T/UM>", KVMessage.StatusType.PUT);
        String unmarshalledMessage = MessageMarshaller.marshall(kvMessage);

        String expected = "PUT" + RECORD_SEPARATOR + "<N<ame/," + RECORD_SEPARATOR + ",T/UM>";
        assertEquals(expected, unmarshalledMessage);
    }

}

