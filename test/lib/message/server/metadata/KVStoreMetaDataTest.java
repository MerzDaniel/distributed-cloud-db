package lib.message.server.metadata;

import junit.framework.TestCase;
import lib.message.MarshallingException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static lib.Constants.ELEMENT_SEPARATOR;
import static lib.Constants.RECORD_SEPARATOR;


public class KVStoreMetaDataTest extends TestCase {

    @Test
    public void testMarshalling() throws MarshallingException {
        List<ServerData> serverList = Arrays.asList(new ServerData("server1", "127.0.0.1", 45000, BigInteger.ZERO),
                new ServerData("server2", "127.0.0.2", 35000, new BigInteger("10001")),
                new ServerData("server3", "127.0.0.3", 50000, new BigInteger("20001")),
                new ServerData("server4", "127.0.0.4", 60000, new BigInteger("30001")));

        KVStoreMetaData kvStoreMetaData = new KVStoreMetaData(serverList);
        KVStoreMetaData result = KVStoreMetaData.unmarshall(kvStoreMetaData.marshall());

        assertEquals(kvStoreMetaData.getKvServerList().size(), result.getKvServerList().size());
    }

    @Test(expected = MarshallingException.class)
    public void testUnMarshallKVStoreMetaDataThrowsException() throws MarshallingException {
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

