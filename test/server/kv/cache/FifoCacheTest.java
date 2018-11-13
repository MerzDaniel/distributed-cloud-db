package server.kv.cache;

import org.junit.Before;
import org.junit.Test;
import server.kv.KeyNotFoundException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class FifoCacheTest {
    FifoCachedKeyValueStore fifoCache;

    @Before
    public void initCache() {
        fifoCache = new FifoCachedKeyValueStore(3, "db");
        this.populateCache();
    }

    private void populateCache() {
        for (int i = 0; i < 2; i++) {
            fifoCache.addToCache("key" + i, "value" + i);
        }
    }

    @Test
    public void addToCacheTest() {
        String key = "testKey";
        String value = "testValue";

        fifoCache.addToCache(key, value);

        assertTrue(fifoCache.cache.containsKey(key));
    }

    @Test
    public void getFromCacheForExistingKeyTest() throws KeyNotFoundException {
        String key = "key1";

        String value = fifoCache.getFromCache(key);

        assertEquals("value1", value);
    }

    @Test(expected = KeyNotFoundException.class)
    public void getFromCacheForNonExistingKeyTest() throws KeyNotFoundException{
        String key = "key68";

        String value = fifoCache.getFromCache(key);
    }

    @Test
    public void addToCacheReplaceTest() throws KeyNotFoundException{
        fifoCache.addToCache("key2", "value2");//cache is full

        fifoCache.getFromCache("key2");
        fifoCache.getFromCache("key0");


        String key = "testKey";
        String value = "testValue";

        fifoCache.addToCache(key, value);

        assertTrue(fifoCache.cache.containsKey(key));
        assertTrue(!fifoCache.cache.containsKey("key0"));
    }
}
