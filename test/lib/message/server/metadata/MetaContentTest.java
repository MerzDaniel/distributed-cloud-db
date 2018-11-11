package lib.message.server.metadata;

import junit.framework.TestCase;
import lib.message.UnmarshallException;
import lib.server.metadata.KVStoreMetaData;
import org.junit.Test;


public class MetaContentTest extends TestCase {

    @Test
    public void testMarshallKVServerMetaData() {
        KVStoreMetaData.MetaContent metaContent = new KVStoreMetaData.MetaContent("127.0.0.1", "45000", 0, 10000);

        String marshalledString = KVStoreMetaData.MetaContent.marshall(metaContent);

        final String ELEMENT_SEPARATOR = "\u001F";
        String expected = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000";

        assertEquals(expected, marshalledString);

    }

    @Test
    public void testUnMarshallKVServerMetaData() throws UnmarshallException {
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000";

        KVStoreMetaData.MetaContent metaContent = KVStoreMetaData.MetaContent.unmarshall(kvStoreMetaDataString);

        assertEquals(metaContent, new KVStoreMetaData.MetaContent("127.0.0.1", "45000", 0, 10000));
    }

    @Test(expected = UnmarshallException.class)
    public void testUnMarshallKVServerMetaDataThrowsException() throws UnmarshallException {
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "qwe12" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000";

        KVStoreMetaData kvStoreMetaData = KVStoreMetaData.unmarshall(kvStoreMetaDataString);
    }

}

