package server.kv.cache;

import server.kv.KeyNotFoundException;
import server.kv.KeyValueStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a KeyValueStore with an embedded Least Recently Used cache
 */
public class LRUCachedKeyValueStore extends CachedKeyValueStore {
    class CacheEntry {
        public CacheEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key;
        public String value;
    }

    List<String> cachePriority = new LinkedList<>();
    Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public LRUCachedKeyValueStore(int cacheSize, String dataFileName) {
        super(cacheSize, dataFileName);
    }

    public LRUCachedKeyValueStore(int cacheSize, KeyValueStore store) {
        super(cacheSize, store);
    }

    /**
     * add the given {@code key} and {@code value} into the database
     *
     * @param key the {@code key}
     * @param value the {@code value}
     */
    @Override
    protected void addToCache(String key, String value) {
        if (isCached(key)) {
            // update value in cache
            CacheEntry entry = cache.get(key);
            entry.value = value;
            cachePriority.remove(key);
            cachePriority.add(key);
            return;
        }
        if (cache.size() >= this.cacheSize) {
            String leastRecentlyUsedKey = cachePriority.get(0);
            cachePriority.remove(leastRecentlyUsedKey);
            cache.remove(leastRecentlyUsedKey);
        }
        cachePriority.add(key);
        cache.put(key, new CacheEntry(key, value));
    }

    /**
     * returns whether the {@code key} is present or not in the database
     *
     * @param key the {@code key} to be checked
     * @return whether the {@code key} is present or not
     */
    @Override
    protected boolean isCached(String key) {
        return cache.get(key) != null;
    }

    /**
     * returns the value associated with the {@code key} in the database
     *
     * @param key the {@code key} for which the value is required
     * @return the value associated with the {@code key}
     * @throws KeyNotFoundException if any errors occurred while retrieving the value
     */
    @Override
    protected String getFromCache(String key) throws KeyNotFoundException {
        CacheEntry entry = cache.get(key);
        if (entry == null) throw new KeyNotFoundException();
        //make this key the most recently used by moving it to the top
        cachePriority.remove(key);
        cachePriority.add(key);
        return entry.value;
    }

    @Override
    protected void removeFromCache(String key) {
        cache.remove(key);
        cachePriority.remove(key);
    }
}
