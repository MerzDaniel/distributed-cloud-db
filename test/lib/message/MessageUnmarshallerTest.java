package lib.message;

import junit.framework.TestCase;
import lib.metadata.ServerData;
import org.junit.Test;

import java.math.BigInteger;

public class MessageUnmarshallerTest extends TestCase {
    final String RECORD_SEPARATOR = "\u001E";
    final String ELEMENT_SEPARATOR = "\u001F";

    @Test
    public void testUnmarshall() throws MarshallingException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "Name" + RECORD_SEPARATOR + "TUM";
        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals("GET", kvMessage.getStatus().name());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TUM", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters1() throws MarshallingException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "<N<ame" + RECORD_SEPARATOR + "<T>UM>";
        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("<N<ame", kvMessage.getKey());
        assertEquals("<T>UM>", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters2() throws MarshallingException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "Name" + RECORD_SEPARATOR + "TU,M";
        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TU,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters3() throws MarshallingException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "Name" + RECORD_SEPARATOR + "TU/,M";
        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters4() throws MarshallingException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "Name/" + RECORD_SEPARATOR + "TU/,M";
        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name/", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters5() throws MarshallingException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "N/,ame," + RECORD_SEPARATOR + "TU/,M";
        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("N/,ame,", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters6() throws MarshallingException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "N/,ame," + RECORD_SEPARATOR + "/,TU/,M";
        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("N/,ame,", kvMessage.getKey());
        assertEquals("/,TU/,M", kvMessage.getValue());
    }

    @Test(expected = MarshallingException.class)
    public void testInvalidMessage() {
        String kvMessageString = "INVALIDMESSAGETYPE" + RECORD_SEPARATOR + "key" + RECORD_SEPARATOR + "value";
        try {
            KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);
        } catch (MarshallingException e) {
            return;
        }
        assertTrue("MarshallingException expected", false);
    }

    @Test
    public void testEmptyMessage() throws MarshallingException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "" + RECORD_SEPARATOR + "";
        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals(null, kvMessage.getKey());
        assertEquals(null, kvMessage.getValue());
    }

    @Test
    public void testMessageWithSpaces() throws MarshallingException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + " " + RECORD_SEPARATOR + " ";
        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals(" ", kvMessage.getKey());
        assertEquals(" ", kvMessage.getValue());
    }


    @Test
    public void testMessageWithEmptyValue() throws MarshallingException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "SampleKey" + RECORD_SEPARATOR + "";
        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("SampleKey", kvMessage.getKey());
        assertEquals(null, kvMessage.getValue());
    }

    @Test
    public void testServerNotFoundMessage() throws MarshallingException {
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000"
                + RECORD_SEPARATOR
                + "127.0.0.2" + ELEMENT_SEPARATOR + "35000" + ELEMENT_SEPARATOR + "10001" + ELEMENT_SEPARATOR + "20000"
                + RECORD_SEPARATOR
                + "127.0.0.3" + ELEMENT_SEPARATOR + "50000" + ELEMENT_SEPARATOR + "20001" + ELEMENT_SEPARATOR + "30000"
                + RECORD_SEPARATOR
                + "127.0.0.4" + ELEMENT_SEPARATOR + "60000" + ELEMENT_SEPARATOR + "30001" + ELEMENT_SEPARATOR + "40000";

        String kvMessageString = "SERVER_NOT_RESPONSIBLE" + RECORD_SEPARATOR + "SampleKey" + RECORD_SEPARATOR + kvStoreMetaDataString;

        KVMessage kvMessage = (KVMessage) MessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.SERVER_NOT_RESPONSIBLE, kvMessage.getStatus());
        assertEquals("SampleKey", kvMessage.getKey());
        assertEquals(kvStoreMetaDataString, kvMessage.getValue());
    }

    @Test
    public void testUnmarshallConfigureMessage() throws MarshallingException {
        String s = "CONFIGURE" + RECORD_SEPARATOR +
                "server" + ELEMENT_SEPARATOR +
                "127.0.0.1" + ELEMENT_SEPARATOR +
                "50001" + ELEMENT_SEPARATOR+
                "000000";
        KVAdminMessage message = (KVAdminMessage) MessageMarshaller.unmarshall(s);
        assertEquals(KVAdminMessage.StatusType.CONFIGURE, message.status);
        ServerData c = message.meta.getKvServerList().get(0);
        assertEquals("127.0.0.1", c.getHost());
        assertEquals(50001, c.getPort());
        assertEquals(BigInteger.ZERO, c.getFromHash());
    }

    @Test
    public void testUnmarshallMoveMessage() throws MarshallingException {
        String s = "MOVE" + RECORD_SEPARATOR +
                "server" + ELEMENT_SEPARATOR +
                "127.0.0.1" + ELEMENT_SEPARATOR +
                "50001" + ELEMENT_SEPARATOR+
                "000000";
        KVAdminMessage message = (KVAdminMessage) MessageMarshaller.unmarshall(s);
        assertEquals(KVAdminMessage.StatusType.MOVE, message.status);
        ServerData c = message.serverData;
        assertEquals("127.0.0.1", c.getHost());
        assertEquals(50001, c.getPort());
        assertEquals(BigInteger.ZERO, c.getFromHash());
    }
}

