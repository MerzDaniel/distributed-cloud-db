package server.kv;

import org.junit.Before;
import org.junit.Test;
import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.kv.KeyValueStore;
import server.kv.SimpleKeyValueStore;
import util.StringBufferReader;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class LFUCacheTest {
    LFUCachedKeyValueStore lfuCache;

    @Before
    public void initCache() {
        lfuCache = new LFUCachedKeyValueStore(100);
        this.populateCache();
    }

    private void populateCache() {
        for (int i = 1; i < 50; i++) {
            lfuCache.addToCache("key" + i, "value" + i);
        }
    }

    @Test
    public void addToCacheTest() {
        String key = "testKey";
        String value = "testValue";

        lfuCache.addToCache(key, value);

        assertTrue(lfuCache.cache.containsKey(key));
    }

    @Test
    public void getFromCacheForExistingKeyTest() throws KeyNotFoundException{
        String key = "key45";

        String value = lfuCache.getFromCache(key);

        assertEquals("value45", value);
    }

    @Test(expected = KeyNotFoundException.class)
    public void getFromCacheForNonExistingKeyTest() throws KeyNotFoundException{
        String key = "key68";

        String value = lfuCache.getFromCache(key);
    }
}
