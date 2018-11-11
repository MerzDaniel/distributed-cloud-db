package lib.message.server.metadata;

import junit.framework.TestCase;
import lib.message.UnmarshallException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.MetaContent;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;


public class KVStoreMetaDataTest extends TestCase {

    @Test
    public void testMarshallKVStoreMetaData() {
        List<MetaContent> serverList = Arrays.asList(new MetaContent("127.0.0.1", 45000, 0, 10000),
                new MetaContent("127.0.0.2", 35000, 10001, 20000),
                new MetaContent("127.0.0.3", 50000, 20001, 30000),
                new MetaContent("127.0.0.4", 60000, 30001, 40000));

        KVStoreMetaData kvStoreMetaData = new KVStoreMetaData(serverList);

        String marshalledString = KVStoreMetaData.marshall(kvStoreMetaData);

        final String RECORD_SEPARATOR = "\u001E";
        final String ELEMENT_SEPARATOR = "\u001F";
        String expected = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000"
                + RECORD_SEPARATOR
                + "127.0.0.2" + ELEMENT_SEPARATOR + "35000" + ELEMENT_SEPARATOR + "10001" + ELEMENT_SEPARATOR + "20000"
                + RECORD_SEPARATOR
                + "127.0.0.3" + ELEMENT_SEPARATOR + "50000" + ELEMENT_SEPARATOR + "20001" + ELEMENT_SEPARATOR + "30000"
                + RECORD_SEPARATOR
                + "127.0.0.4" + ELEMENT_SEPARATOR + "60000" + ELEMENT_SEPARATOR + "30001" + ELEMENT_SEPARATOR + "40000";

        assertEquals(expected, marshalledString);

    }

    @Test
    public void testUnMarshallKVStoreMetaData() throws UnmarshallException {
        final String RECORD_SEPARATOR = "\u001E";
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000"
                + RECORD_SEPARATOR
                + "127.0.0.2" + ELEMENT_SEPARATOR + "35000" + ELEMENT_SEPARATOR + "10001" + ELEMENT_SEPARATOR + "20000"
                + RECORD_SEPARATOR
                + "127.0.0.3" + ELEMENT_SEPARATOR + "50000" + ELEMENT_SEPARATOR + "20001" + ELEMENT_SEPARATOR + "30000"
                + RECORD_SEPARATOR
                + "127.0.0.4" + ELEMENT_SEPARATOR + "60000" + ELEMENT_SEPARATOR + "30001" + ELEMENT_SEPARATOR + "40000";

        KVStoreMetaData kvStoreMetaData = KVStoreMetaData.unmarshall(kvStoreMetaDataString);
        System.out.println("");
        assertEquals(kvStoreMetaData.getKvServerList().get(0), new MetaContent("127.0.0.1", 45000, 0, 10000));
        assertEquals(kvStoreMetaData.getKvServerList().get(1), new MetaContent("127.0.0.2", 35000, 10001, 20000));
        assertEquals(kvStoreMetaData.getKvServerList().get(2), new MetaContent("127.0.0.3", 50000, 20001, 30000));
        assertEquals(kvStoreMetaData.getKvServerList().get(3), new MetaContent("127.0.0.4", 60000, 30001, 40000));
    }

    @Test(expected = UnmarshallException.class)
    public void testUnMarshallKVStoreMetaDataThrowsException() throws UnmarshallException {
        final String RECORD_SEPARATOR = "\u001E";
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "qwe12" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000"
                + RECORD_SEPARATOR
                + "127.0.0.2" + ELEMENT_SEPARATOR + "35000" + ELEMENT_SEPARATOR + "10001" + ELEMENT_SEPARATOR + "20000"
                + RECORD_SEPARATOR
                + "127.0.0.3" + ELEMENT_SEPARATOR + "50000" + ELEMENT_SEPARATOR + "20001" + ELEMENT_SEPARATOR + "30000"
                + RECORD_SEPARATOR
                + "127.0.0.4" + ELEMENT_SEPARATOR + "60000" + ELEMENT_SEPARATOR + "30001" + ELEMENT_SEPARATOR + "40000";

        KVStoreMetaData kvStoreMetaData = KVStoreMetaData.unmarshall(kvStoreMetaDataString);
    }
}
