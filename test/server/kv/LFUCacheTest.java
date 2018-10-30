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
        lfuCache = new LFUCachedKeyValueStore(3);
        this.populateCache();
    }

    private void populateCache() {
        for (int i = 0; i < 2; i++) {
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
        String key = "key1";

        String value = lfuCache.getFromCache(key);

        assertEquals("value1", value);
    }

    @Test(expected = KeyNotFoundException.class)
    public void getFromCacheForNonExistingKeyTest() throws KeyNotFoundException{
        String key = "key68";

        String value = lfuCache.getFromCache(key);
    }

    @Test
    public void addToCacheReplaceTest() throws KeyNotFoundException{
        lfuCache.addToCache("key2", "value2");//cache is full

        //set the hit count of the cached entries
        for (int i = 0; i < 2; i++) {
            lfuCache.getFromCache("key2");
        }

        lfuCache.getFromCache("key0");


        String key = "testKey";
        String value = "testValue";

        lfuCache.addToCache(key, value);

        assertTrue(lfuCache.cache.containsKey(key));
        assertTrue(!lfuCache.cache.containsKey("key1"));
    }
}
