package server.kv;

import org.junit.Ignore;
import org.junit.Test;
import util.StringBufferReader;

import java.io.*;

import static junit.framework.Assert.assertEquals;

public class SimpleKeyValueStoreTest {
    private final String RECORD_SEPARATOR = "\u001E";
    private final String DATA_FILE_NAME = "db";

    @Test
    public void shouldGetValue() throws KeyNotFoundException, DbError {
        Reader reader = new StringReader("ab" + RECORD_SEPARATOR + "c\nde" + RECORD_SEPARATOR + "fg\na" + RECORD_SEPARATOR + "ha");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer, DATA_FILE_NAME);

        assertEquals("fg", sv.get("de"));
    }

    @Test
    public void shouldGetMultipleValues() throws KeyNotFoundException, DbError {
        Reader reader = new StringReader("ab" + RECORD_SEPARATOR + "c\nde" + RECORD_SEPARATOR + "fg\na" + RECORD_SEPARATOR + "ha");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer, DATA_FILE_NAME);

        assertEquals("fg", sv.get("de"));
        assertEquals("ha", sv.get("a"));
        assertEquals("c", sv.get("ab"));
    }

    @Test
    public void shouldReturnCorrectValuesForHasKey() throws KeyNotFoundException, DbError {
        Reader reader = new StringReader("ab" + RECORD_SEPARATOR + "c\nde" + RECORD_SEPARATOR + "fg\na" + RECORD_SEPARATOR + "ha");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer, DATA_FILE_NAME);

        assertEquals(true, sv.hasKey("de"));
        assertEquals(false, sv.hasKey("non-existing-key"));
    }

    @Test(expected = KeyNotFoundException.class)
    public void shouldThrowKeyNotFound() throws KeyNotFoundException, DbError {
        Reader reader = new StringReader("ab" + RECORD_SEPARATOR + "c\nde" + RECORD_SEPARATOR + "fg\na" + RECORD_SEPARATOR + "ha");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer, DATA_FILE_NAME);

        sv.get("non-existing-key");
    }

    @Test
    @Ignore
    public void shouldWriteValues() throws DbError {
        Reader reader = new StringReader("");
        Writer writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer, DATA_FILE_NAME);

        sv.put("key", "value");
        assertEquals("key=value\n", ((StringWriter) writer).getBuffer().toString());
    }

    @Test
    @Ignore
    public void readWrites() throws DbError, KeyNotFoundException {
        StringBuffer buf = new StringBuffer();
        Reader reader = new StringBufferReader(buf);
        StringWriter writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer, DATA_FILE_NAME);

        sv.put("key", "value");
        buf.append(writer.getBuffer());

        assertEquals("value", sv.get("key"));
    }

    @Test
    @Ignore
    public void readMultipleWrites() throws DbError, KeyNotFoundException {
        StringBuffer buf = new StringBuffer();
        Reader reader = new StringBufferReader(buf);
        StringWriter writer = new StringWriter();
        KeyValueStore sv = new SimpleKeyValueStore(reader, writer, DATA_FILE_NAME);

        sv.put("key", "value");
        sv.put("another-key", "another-value");
        buf.append(writer.getBuffer());

        assertEquals("value", sv.get("key"));
        assertEquals("another-value", sv.get("another-key"));
    }
}
