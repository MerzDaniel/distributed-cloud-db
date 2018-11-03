package server.kv;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class RandomAccessKeyValueStoreTest {
    File dbFile = new File(Paths.get("test_files", "TEST_DB").toUri());
    RandomAccessKeyValueStore kvStore;
    @Before
    public void setup() throws IOException {
        kvStore = new RandomAccessKeyValueStore(dbFile);
        kvStore.init();
    }
    @After
    public void tearDown() throws Exception {
        kvStore.shutdown();
        if (!dbFile.delete()) throw new Exception("What the hell");
    }

    @Test
    public void shouldGetValue() throws KeyNotFoundException, DbError {
        kvStore.put("de", "fg");

        assertEquals("fg", kvStore.get("de"));
    }

    @Test
    public void shouldGetMultipleValues() throws KeyNotFoundException, DbError {
        kvStore.put("ab", "c");
        kvStore.put("de", "fg");
        kvStore.put("asdfkj", "fg");
        kvStore.put("riwero", "fg");
        kvStore.put("a", "ha");

        assertEquals("fg", kvStore.get("de"));
        assertEquals("ha", kvStore.get("a"));
        assertEquals("c", kvStore.get("ab"));
    }

    @Test
    public void shouldReturnCorrectValuesForHasKey() throws KeyNotFoundException, DbError {
        kvStore.put("ab", "c");
        kvStore.put("de", "fg");
        kvStore.put("asdfkj", "fg");
        kvStore.put("riwero", "fg");
        kvStore.put("a", "ha");

        assertEquals(true, kvStore.hasKey("de"));
        assertEquals(false, kvStore.hasKey("non-existing-key"));
    }

    @Test(expected = KeyNotFoundException.class)
    public void shouldThrowKeyNotFound() throws KeyNotFoundException, DbError {
        kvStore.put("ab", "c");
        kvStore.put("a", "ha");

        kvStore.get("non-existing-key");
    }

    @Test
    public void shouldWriteFile() throws DbError {
        assertEquals(0, dbFile.length());
        kvStore.put("key", "value");
        assertTrue(dbFile.length() > 0);
    }

    @Test
    public void shouldDeleteValue() throws DbError {
        kvStore.put("abc", "abc");
        kvStore.deleteKey("abc");
        assertFalse(kvStore.hasKey("abc"));
    }

    @Test
    public void shouldDeleteValueAndReadOthers() throws DbError {
        kvStore.put("abc", "abc");
        kvStore.put("a", "a");
        kvStore.deleteKey("abc");
        assertFalse(kvStore.hasKey("abc"));
        assertTrue("Other key was not found anymore", kvStore.hasKey("a"));
    }

    @Test
    public void shouldUpdateValue() throws DbError, KeyNotFoundException {
        kvStore.put("a", "a");
        boolean result = kvStore.put("a", "b");
        String val = kvStore.get("a");
        assertTrue("Should be updated", result);
        assertTrue("New value should be returned", val.equals("b"));
    }
}
