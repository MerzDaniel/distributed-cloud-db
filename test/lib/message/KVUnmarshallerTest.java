package lib.message;

import junit.framework.TestCase;
import org.junit.Test;

public class KVUnmarshallerTest extends TestCase {

    @Test
    public void testUnmarshall() throws UnmarshallException {
        String kvMessageString = "GET<Name,TUM>";
        KVMessage kvMessage = KVMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals("GET", kvMessage.getStatus().name());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TUM", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters1() throws UnmarshallException {
        String kvMessageString = "GET</<N/<ame,/<T/>UM/>>";
        KVMessage kvMessage = KVMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("<N<ame", kvMessage.getKey());
        assertEquals("<T>UM>", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters2() throws UnmarshallException {
        String kvMessageString = "GET<Name,TU/,M>";
        KVMessage kvMessage = KVMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TU,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters3() throws UnmarshallException {
        String kvMessageString = "GET<Name,TU///,M>";
        KVMessage kvMessage = KVMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters4() throws UnmarshallException {
        String kvMessageString = "GET<Name//,TU///,M>";
        KVMessage kvMessage = KVMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name/", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters5() throws UnmarshallException {
        String kvMessageString = "GET<N///,ame/,,TU///,M>";
        KVMessage kvMessage = KVMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("N/,ame,", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters6() throws UnmarshallException {
        String kvMessageString = "GET<N///,ame/,,///,TU///,M>";
        KVMessage kvMessage = KVMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("N/,ame,", kvMessage.getKey());
        assertEquals("/,TU/,M", kvMessage.getValue());
    }

    @Test(expected = UnmarshallException.class)
    public void testInvalidMessage() {
        String kvMessageString = "INVALIDMESSAGETYPE<key,value>";
        try {
            KVMessage kvMessage = KVMessageUnmarshaller.unmarshall(kvMessageString);
        } catch (UnmarshallException e) {
            return;
        }
        assertTrue("UnmarshallException expected", false);
    }

    @Test
    public void testEmptyMessage() throws UnmarshallException {
        String kvMessageString = "GET<,>";
        KVMessage kvMessage = KVMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals(null, kvMessage.getKey());
        assertEquals(null, kvMessage.getValue());
    }

    @Test
    public void testMessageWithSpaces() throws UnmarshallException {
        String kvMessageString = "GET< , >";
        KVMessage kvMessage = KVMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals(" ", kvMessage.getKey());
        assertEquals(" ", kvMessage.getValue());
    }
}

