package server.kv;

import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static junit.framework.Assert.assertEquals;

public class SimpleKeyValueStoreTest {
    @Test
    public void shouldGetValue() throws KeyNotFoundException, DbError {
        Reader reader = new StringReader("ab=c\nde=fg\na=ha");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer);

        assertEquals("fg", sv.get("de"));
    }

    @Test
    public void shouldGetMultipleValues() throws KeyNotFoundException, DbError {
        Reader reader = new StringReader("ab=c\nde=fg\na=ha");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer);

        assertEquals("fg", sv.get("de"));
        assertEquals("ha", sv.get("a"));
        assertEquals("c", sv.get("ab"));
    }

    @Test(expected = KeyNotFoundException.class)
    public void shouldThrowKeyNotFound() throws KeyNotFoundException, DbError {
        Reader reader = new StringReader("ab=c\nde=fg\na=ha");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer);

        sv.get("non-existing-key");
    }
}
