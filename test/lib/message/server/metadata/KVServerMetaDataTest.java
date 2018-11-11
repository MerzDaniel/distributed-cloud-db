package lib.message.server.metadata;

import junit.framework.TestCase;
import lib.message.UnmarshallException;
import lib.server.metadata.KVStoreMetaData;
import lib.server.metadata.KVStoreMetaDataUtil;
import org.junit.Test;
import server.KVServer;

import java.util.Arrays;
import java.util.List;


public class KVServerMetaDataTest extends TestCase {

    @Test
    public void testMarshallKVServerMetaData() {
        KVStoreMetaData.KVServerMetaData kvServerMetaData = new KVStoreMetaData.KVServerMetaData("127.0.0.1", "45000", 0, 10000);

        String marshalledString = KVStoreMetaData.KVServerMetaData.marshall(kvServerMetaData);

        final String ELEMENT_SEPARATOR = "\u001F";
        String expected = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000";

        assertEquals(expected, marshalledString);

    }

    @Test
    public void testUnMarshallKVServerMetaData() throws UnmarshallException {
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000";

        KVStoreMetaData.KVServerMetaData kvServerMetaData = KVStoreMetaData.KVServerMetaData.unmarshall(kvStoreMetaDataString);

        assertEquals(kvServerMetaData, new KVStoreMetaData.KVServerMetaData("127.0.0.1", "45000", 0, 10000));
    }

    @Test(expected = UnmarshallException.class)
    public void testUnMarshallKVServerMetaDataThrowsException() throws UnmarshallException {
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "qwe12" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000";

        KVStoreMetaData kvStoreMetaData = KVStoreMetaData.unmarshall(kvStoreMetaDataString);
    }

}

