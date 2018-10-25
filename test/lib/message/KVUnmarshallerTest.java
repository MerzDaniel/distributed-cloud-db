package lib.message;

import junit.framework.TestCase;
import lib.message.KVMessage;
import lib.message.KVMessageUnmarshaller;
import org.junit.Test;

public class KVUnmarshallerTest extends TestCase {

    @Test
    public void testUnmarshall() {
        String kvMessageString = "GET<Name,TUM>";
        KVMessageUnmarshaller kvMessageUnmarshaller = new KVMessageUnmarshaller();
        KVMessage kvMessage = kvMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals("GET",kvMessage.getStatus().name());
        assertEquals("Name",kvMessage.getKey());
        assertEquals("TUM",kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters1() {
        String kvMessageString = "GET</<N/<ame,/<T/>UM/>>";
        KVMessageUnmarshaller kvMessageUnmarshaller = new KVMessageUnmarshaller();
        KVMessage kvMessage = kvMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("<N<ame", kvMessage.getKey());
        assertEquals("<T>UM>", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters2() {
        String kvMessageString = "GET<Name,TU/,M>";
        KVMessageUnmarshaller kvMessageUnmarshaller = new KVMessageUnmarshaller();
        KVMessage kvMessage = kvMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TU,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters3() {
        String kvMessageString = "GET<Name,TU///,M>";
        KVMessageUnmarshaller kvMessageUnmarshaller = new KVMessageUnmarshaller();
        KVMessage kvMessage = kvMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters4() {
        String kvMessageString = "GET<Name//,TU///,M>";
        KVMessageUnmarshaller kvMessageUnmarshaller = new KVMessageUnmarshaller();
        KVMessage kvMessage = kvMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("Name/", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters5() {
        String kvMessageString = "GET<N///,ame/,,TU///,M>";
        KVMessageUnmarshaller kvMessageUnmarshaller = new KVMessageUnmarshaller();
        KVMessage kvMessage = kvMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("N/,ame,", kvMessage.getKey());
        assertEquals("TU/,M", kvMessage.getValue());
    }

    @Test
    public void testUnmarshallScpecialCharacters6() {
        String kvMessageString = "GET<N///,ame/,,///,TU///,M>";
        KVMessageUnmarshaller kvMessageUnmarshaller = new KVMessageUnmarshaller();
        KVMessage kvMessage = kvMessageUnmarshaller.unmarshall(kvMessageString);

        assertEquals(KVMessage.StatusType.GET, kvMessage.getStatus());
        assertEquals("N/,ame,", kvMessage.getKey());
        assertEquals("/,TU/,M", kvMessage.getValue());
    }

}
