package lib.message.server.metadata;

import junit.framework.TestCase;
import lib.message.MarshallingException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.junit.Test;

import java.math.BigInteger;


public class ServerDataTest extends TestCase {

    @Test
    public void testMarshallKVServerMetaData() {
        ServerData serverData = new ServerData("127.0.0.1", 45000, BigInteger.ZERO);

        String marshalledString = serverData.marshall();

        final String ELEMENT_SEPARATOR = "\u001F";
        String expected = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0";

        assertEquals(expected, marshalledString);

    }

    @Test
    public void testUnMarshallKVServerMetaData() throws MarshallingException {
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000";

        ServerData serverData = ServerData.unmarshall(kvStoreMetaDataString);

        assertEquals(serverData, new ServerData("127.0.0.1", 45000, BigInteger.ZERO));
    }

    @Test(expected = MarshallingException.class)
    public void testUnMarshallKVServerMetaDataThrowsException() {
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "qwe12" + ELEMENT_SEPARATOR + "0";


        try {
            KVStoreMetaData kvStoreMetaData = KVStoreMetaData.unmarshall(kvStoreMetaDataString);
        } catch (MarshallingException e) {
            return;
        }
        assertTrue(false);
    }

}

