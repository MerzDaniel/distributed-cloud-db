package server.kv;

import org.junit.Test;

import java.io.*;

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

    @Test
    public void shouldReturnCorrectValuesForHasKey() throws KeyNotFoundException, DbError {
        Reader reader = new StringReader("ab=c\nde=fg\na=ha");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer);

        assertEquals(true, sv.hasKey("de"));
        assertEquals(false, sv.hasKey("non-existing-key"));
    }

    @Test(expected = KeyNotFoundException.class)
    public void shouldThrowKeyNotFound() throws KeyNotFoundException, DbError {
        Reader reader = new StringReader("ab=c\nde=fg\na=ha");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer);

        sv.get("non-existing-key");
    }

    @Test
    public void shouldWriteValues() throws DbError {
        Reader reader = new StringReader("");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer);

        sv.put("key", "value");
        assertEquals("key=value\n", ((StringWriter) writer).getBuffer().toString());
    }

    @Test
    public void readWrites() throws DbError, KeyNotFoundException {
        StringBuffer buf = new StringBuffer();
        Reader reader = new StringBufferReader(buf);
        StringWriter writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer);

        sv.put("key", "value");
        buf.append(writer.getBuffer());

        assertEquals("value", sv.get("key"));
    }

    @Test
    public void readMultipleWrites() throws DbError, KeyNotFoundException {
        StringBuffer buf = new StringBuffer();
        Reader reader = new StringBufferReader(buf);
        StringWriter writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer);

        sv.put("key", "value");
        sv.put("another-key", "another-value");
        buf.append(writer.getBuffer());

        assertEquals("value", sv.get("key"));
        assertEquals("another-value", sv.get("another-key"));
    }
}
