package lib.message;

import com.sun.org.apache.regexp.internal.RE;
import junit.framework.TestCase;
import org.junit.Test;

public class KVUnmarshallerTest extends TestCase {
    final String RECORD_SEPARATOR = "\u001E";
    final String ELEMENT_SEPARATOR = "\u001F";

    @Test
    public void testUnmarshall() throws UnmarshallException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "Name" + RECORD_SEPARATOR + "TUM";
        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals("GET", kvMessage.getStatus().name());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TUM", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters1() throws UnmarshallException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "<N<ame" + RECORD_SEPARATOR + "<T>UM>";
        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("<N<ame", kvMessage.getKey());
        assertEquals("<T>UM>", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters2() throws UnmarshallException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "Name" + RECORD_SEPARATOR + "TU,M";
        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TU,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters3() throws UnmarshallException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "Name" + RECORD_SEPARATOR + "TU/,M";
        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters4() throws UnmarshallException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "Name/" + RECORD_SEPARATOR + "TU/,M";
        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name/", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters5() throws UnmarshallException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "N/,ame," + RECORD_SEPARATOR + "TU/,M";
        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("N/,ame,", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters6() throws UnmarshallException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "N/,ame," + RECORD_SEPARATOR + "/,TU/,M";
        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("N/,ame,", kvMessage.getKey());
        assertEquals("/,TU/,M", kvMessage.getValue());
    }

    @Test(expected = UnmarshallException.class)
    public void testInvalidMessage() {
        String kvMessageString = "INVALIDMESSAGETYPE" + RECORD_SEPARATOR + "key" + RECORD_SEPARATOR + "value";
        try {
            KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);
        } catch (UnmarshallException e) {
            return;
        }
        assertTrue("UnmarshallException expected", false);
    }

    @Test
    public void testEmptyMessage() throws UnmarshallException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "" + RECORD_SEPARATOR + "";
        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals(null, kvMessage.getKey());
        assertEquals(null, kvMessage.getValue());
    }

    @Test
    public void testMessageWithSpaces() throws UnmarshallException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + " " + RECORD_SEPARATOR + " ";
        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals(" ", kvMessage.getKey());
        assertEquals(" ", kvMessage.getValue());
    }


    @Test
    public void testMessageWithEmptyValue() throws UnmarshallException {
        String kvMessageString = "GET" + RECORD_SEPARATOR + "SampleKey" + RECORD_SEPARATOR + "";
        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("SampleKey", kvMessage.getKey());
        assertEquals(null, kvMessage.getValue());
    }

    @Test
    public void testServerNotFoundMessage() throws UnmarshallException {
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000"
                + RECORD_SEPARATOR
                + "127.0.0.2" + ELEMENT_SEPARATOR + "35000" + ELEMENT_SEPARATOR + "10001" + ELEMENT_SEPARATOR + "20000"
                + RECORD_SEPARATOR
                + "127.0.0.3" + ELEMENT_SEPARATOR + "50000" + ELEMENT_SEPARATOR + "20001" + ELEMENT_SEPARATOR + "30000"
                + RECORD_SEPARATOR
                + "127.0.0.4" + ELEMENT_SEPARATOR + "60000" + ELEMENT_SEPARATOR + "30001" + ELEMENT_SEPARATOR + "40000";

        String kvMessageString = "SERVER_NOT_RESPONSIBLE" + RECORD_SEPARATOR + "SampleKey" + RECORD_SEPARATOR + kvStoreMetaDataString;

        KVMessage kvMessage = KVMessageMarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.SERVER_NOT_RESPONSIBLE, kvMessage.getStatus());
        assertEquals("SampleKey", kvMessage.getKey());
        assertEquals(kvStoreMetaDataString, kvMessage.getValue());
    }
}

