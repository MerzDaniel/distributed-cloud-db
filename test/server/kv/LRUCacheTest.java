package server.kv;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class LRUCacheTest {
    LRUCachedKeyValueStore lruCache;

    @Before
    public void initCache() {
        lruCache = new LRUCachedKeyValueStore(3);
        this.populateCache();
    }

    private void populateCache() {
        for (int i = 0; i < 2; i++) {
            lruCache.addToCache("key" + i, "value" + i);
        }
    }

    @Test
    public void addToCacheTest() {
        String key = "testKey";
        String value = "testValue";

        lruCache.addToCache(key, value);

        assertTrue(lruCache.cache.containsKey(key));
    }

    @Test
    public void getFromCacheForExistingKeyTest() throws KeyNotFoundException{
        String key = "key1";

        String value = lruCache.getFromCache(key);

        assertEquals("value1", value);
    }

    @Test(expected = KeyNotFoundException.class)
    public void getFromCacheForNonExistingKeyTest() throws KeyNotFoundException{
        String key = "key68";

        String value = lruCache.getFromCache(key);
    }

    @Test
    public void addToCacheReplaceLogicTest() throws KeyNotFoundException{
        lruCache.addToCache("key2", "value2");//cache is full

        //modify recently used details for entries
        lruCache.getFromCache("key2");
        lruCache.getFromCache("key0");


        String key = "testKey";
        String value = "testValue";

        lruCache.addToCache(key, value);

        assertTrue(lruCache.cache.containsKey(key));
        assertTrue(!lruCache.cache.containsKey("key1"));
    }
}
