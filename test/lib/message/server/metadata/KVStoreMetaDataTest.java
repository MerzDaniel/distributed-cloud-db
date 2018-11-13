package lib.message.server.metadata;

import junit.framework.TestCase;
import lib.message.MarshallingException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;


public class KVStoreMetaDataTest extends TestCase {

    @Test
    public void testMarshallKVStoreMetaData() {
        List<ServerData> serverList = Arrays.asList(new ServerData("server1", "127.0.0.1", 45000, BigInteger.ZERO),
                new ServerData("server2", "127.0.0.2", 35000, new BigInteger("10001")),
                new ServerData("server3", "127.0.0.3", 50000, new BigInteger("20001")),
                new ServerData("server4", "127.0.0.4", 60000, new BigInteger("30001")));

        KVStoreMetaData kvStoreMetaData = new KVStoreMetaData(serverList);

        String marshalledString = kvStoreMetaData.marshall();

        final String RECORD_SEPARATOR = "\u001E";
        final String ELEMENT_SEPARATOR = "\u001F";
        String expected = "server1" + ELEMENT_SEPARATOR + "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0"
                + RECORD_SEPARATOR
                + "server2" + ELEMENT_SEPARATOR + "127.0.0.2" + ELEMENT_SEPARATOR + "35000" + ELEMENT_SEPARATOR + "10001"
                + RECORD_SEPARATOR
                + "server3" + ELEMENT_SEPARATOR +"127.0.0.3" + ELEMENT_SEPARATOR + "50000" + ELEMENT_SEPARATOR + "20001"
                + RECORD_SEPARATOR
                + "server4" + ELEMENT_SEPARATOR +"127.0.0.4" + ELEMENT_SEPARATOR + "60000" + ELEMENT_SEPARATOR + "30001";

        assertEquals(expected, marshalledString);

    }

    @Test
    public void testUnMarshallKVStoreMetaData() throws MarshallingException {
        final String RECORD_SEPARATOR = "\u001E";
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "server1" + ELEMENT_SEPARATOR + "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000"
                + RECORD_SEPARATOR
                + "server2" + ELEMENT_SEPARATOR + "127.0.0.2" + ELEMENT_SEPARATOR + "35000" + ELEMENT_SEPARATOR + "10001" + ELEMENT_SEPARATOR + "20000"
                + RECORD_SEPARATOR
                + "server3" + ELEMENT_SEPARATOR + "127.0.0.3" + ELEMENT_SEPARATOR + "50000" + ELEMENT_SEPARATOR + "20001" + ELEMENT_SEPARATOR + "30000"
                + RECORD_SEPARATOR
                + "server4" + ELEMENT_SEPARATOR + "127.0.0.4" + ELEMENT_SEPARATOR + "60000" + ELEMENT_SEPARATOR + "30001" + ELEMENT_SEPARATOR + "40000";

        KVStoreMetaData kvStoreMetaData = KVStoreMetaData.unmarshall(kvStoreMetaDataString);
        assertEquals(kvStoreMetaData.getKvServerList().get(0), new ServerData("server1", "127.0.0.1", 45000, BigInteger.ZERO));
        assertEquals(kvStoreMetaData.getKvServerList().get(1), new ServerData("server2", "127.0.0.2", 35000, new BigInteger("10001")));
        assertEquals(kvStoreMetaData.getKvServerList().get(2), new ServerData("server3", "127.0.0.3", 50000, new BigInteger("20001")));
        assertEquals(kvStoreMetaData.getKvServerList().get(3), new ServerData("server4", "127.0.0.4", 60000, new BigInteger("30001")));
    }

    @Test(expected = MarshallingException.class)
    public void testUnMarshallKVStoreMetaDataThrowsException() throws MarshallingException {
        final String RECORD_SEPARATOR = "\u001E";
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "server1" + ELEMENT_SEPARATOR + "127.0.0.1" + ELEMENT_SEPARATOR + "qwe12" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000"
                + RECORD_SEPARATOR
                + "server2" + ELEMENT_SEPARATOR + "127.0.0.2" + ELEMENT_SEPARATOR + "35000" + ELEMENT_SEPARATOR + "10001" + ELEMENT_SEPARATOR + "20000"
                + RECORD_SEPARATOR
                + "server3" + ELEMENT_SEPARATOR + "127.0.0.3" + ELEMENT_SEPARATOR + "50000" + ELEMENT_SEPARATOR + "20001" + ELEMENT_SEPARATOR + "30000"
                + RECORD_SEPARATOR
                + "server4" + ELEMENT_SEPARATOR + "127.0.0.4" + ELEMENT_SEPARATOR + "60000" + ELEMENT_SEPARATOR + "30001" + ELEMENT_SEPARATOR + "40000";

        try {
            KVStoreMetaData kvStoreMetaData = KVStoreMetaData.unmarshall(kvStoreMetaDataString);
        } catch (MarshallingException e) {
            return;
        }

        assertTrue(false);
    }
}

