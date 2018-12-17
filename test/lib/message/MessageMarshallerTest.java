package lib.message;

import junit.framework.TestCase;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.junit.Test;

import static lib.Constants.ELEMENT_SEPARATOR;
import static lib.Constants.RECORD_SEPARATOR;


public class MessageMarshallerTest extends TestCase {

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
        m.meta.getKvServerList().add(new ServerData("server", "localhost", 50000));
        KVAdminMessage result = (KVAdminMessage) MessageMarshaller.unmarshall(m.marshall());

        assertEquals(m.status, result.status);
        assertEquals(m.meta.getKvServerList().size(), result.meta.getKvServerList().size());
    }
}
