package lib.message.server.metadata;

import junit.framework.TestCase;
import lib.message.MarshallingException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.MetaContent;
import org.junit.Test;


public class MetaContentTest extends TestCase {

    @Test
    public void testMarshallKVServerMetaData() {
        MetaContent metaContent = new MetaContent("127.0.0.1", 45000, 0, 10000);

        String marshalledString = MetaContent.marshall(metaContent);

        final String ELEMENT_SEPARATOR = "\u001F";
        String expected = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000";

        assertEquals(expected, marshalledString);

    }

    @Test
    public void testUnMarshallKVServerMetaData() throws MarshallingException {
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "45000" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000";

        MetaContent metaContent = MetaContent.unmarshall(kvStoreMetaDataString);

        assertEquals(metaContent, new MetaContent("127.0.0.1", 45000, 0, 10000));
    }

    @Test(expected = MarshallingException.class)
    public void testUnMarshallKVServerMetaDataThrowsException() throws MarshallingException {
        final String ELEMENT_SEPARATOR = "\u001F";
        String kvStoreMetaDataString = "127.0.0.1" + ELEMENT_SEPARATOR + "qwe12" + ELEMENT_SEPARATOR + "0" + ELEMENT_SEPARATOR + "10000";

        KVStoreMetaData kvStoreMetaData = KVStoreMetaData.unmarshall(kvStoreMetaDataString);
    }

}

