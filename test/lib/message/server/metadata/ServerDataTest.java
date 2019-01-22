package lib.message.server.metadata;

import junit.framework.TestCase;
import lib.message.exception.MarshallingException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.CacheType;
import org.junit.Test;

import java.math.BigInteger;


public class ServerDataTest extends TestCase {

    @Test
    public void testMarshallServerData() {
        ServerData serverData = new ServerData("server", "127.0.0.1", 45000, BigInteger.ZERO, CacheType.FIFO, 20);

        String marshalledString = serverData.marshall();

        final String ELEMENT_SEPARATOR = "\u001F";
        String expected = String.join(
                ELEMENT_SEPARATOR,
                "server", "127.0.0.1", "45000", "0", CacheType.FIFO.name(), "20");

        assertEquals(expected, marshalledString);

    }

    @Test
    public void testUnMarshallServerData() throws MarshallingException {
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = String.join(
                ELEMENT_SEPARATOR,
                "server", "127.0.0.1", "45000", "0", CacheType.FIFO.name(), "20");

        ServerData serverData = ServerData.unmarshall(kvStoreMetaDataString);

        assertEquals(serverData, new ServerData("server", "127.0.0.1", 45000, BigInteger.ZERO, CacheType.FIFO, 20));
    }

    @Test(expected = MarshallingException.class)
    public void testUnMarshallServerDataThrowsException() {
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

