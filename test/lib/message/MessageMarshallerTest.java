package lib.message;

import junit.framework.TestCase;
import lib.metadata.KVStoreMetaData;
import lib.metadata.MetaContent;
import org.junit.Test;


public class MessageMarshallerTest extends TestCase {
    final String RECORD_SEPARATOR = "\u001E";
    final static String ELEMENT_SEPARATOR = "\u001F";

    @Test
    public void testMarshall() throws MarshallingException {
        KVMessage kvMessage = new KVMessageImpl("Name", "TUM", KVMessage.StatusType.PUT);
        String unmarshalledMessage = MessageMarshaller.marshall(kvMessage);

        String expected = "PUT" + RECORD_SEPARATOR + "Name" + RECORD_SEPARATOR + "TUM";
        assertEquals(expected, unmarshalledMessage);
    }

    @Test
    public void testMarshallForNullPair() throws MarshallingException {
        KVMessage kvMessage = new KVMessageImpl(null, null, KVMessage.StatusType.PUT);
        String unmarshalledMessage = MessageMarshaller.marshall(kvMessage);

        String expected = "PUT" + RECORD_SEPARATOR + "" + RECORD_SEPARATOR + "";
        assertEquals(expected, unmarshalledMessage);
    }

    @Test
    public void testMarshallForEmptyPair() throws MarshallingException {
        KVMessage kvMessage = new KVMessageImpl("", "", KVMessage.StatusType.PUT);
        String unmarshalledMessage = MessageMarshaller.marshall(kvMessage);

        String expected = "PUT" + RECORD_SEPARATOR + "" + RECORD_SEPARATOR + "";
        assertEquals(expected, unmarshalledMessage);
    }

    @Test
    public void testMarshallSpecialCharacters() throws MarshallingException {
        KVMessage kvMessage = new KVMessageImpl("<N<ame/,", ",T/UM>", KVMessage.StatusType.PUT);
        String unmarshalledMessage = MessageMarshaller.marshall(kvMessage);

        String expected = "PUT" + RECORD_SEPARATOR + "<N<ame/," + RECORD_SEPARATOR + ",T/UM>";
        assertEquals(expected, unmarshalledMessage);
    }

    @Test
    public void testMarshallAdminConfigureMessage() throws MarshallingException {
        KVAdminMessage m = new KVAdminMessage(KVAdminMessage.StatusType.CONFIGURE);
        m.meta = new KVStoreMetaData();
        m.meta.getKvServerList().add(new MetaContent("localhost", 50000));

        String expected = String.format("CONFIGURE%1$slocalhost%2$s50000%2$s0%2$s0", RECORD_SEPARATOR, ELEMENT_SEPARATOR);
        String result = MessageMarshaller.marshall(m);
        assertEquals(expected , result);
    }
    @Test
    public void testMarshallAdminMoveMessage() throws MarshallingException {
        KVAdminMessage m = new KVAdminMessage(KVAdminMessage.StatusType.MOVE);
        m.meta = new KVStoreMetaData();
        m.metaContent = new MetaContent("localhost", 50000);

        String expected = String.format("MOVE%1$slocalhost%2$s50000%2$s0%2$s0", RECORD_SEPARATOR, ELEMENT_SEPARATOR);
        String result = MessageMarshaller.marshall(m);
        assertEquals(expected , result);
    }
}
