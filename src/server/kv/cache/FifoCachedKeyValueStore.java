package server.kv.cache;

import server.kv.KeyNotFoundException;
import server.kv.KeyValueStore;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a KeyValueStore with an embedded First In Forst Out cache
 */
public class FifoCachedKeyValueStore extends CachedKeyValueStore {
    class CacheEntry {
        public CacheEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key;
        public String value;
    }

    List<String> cachePriority = new LinkedList<>();
    Hashtable<String, CacheEntry> cache = new Hashtable<>();

    public FifoCachedKeyValueStore(int cacheSize) {
        super(cacheSize);
    }

    public FifoCachedKeyValueStore(int cacheSize, KeyValueStore store) {
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
            CacheEntry entry = cache.get(key);
            entry.value = value;
            return;
        }
        if (cache.size() >= this.cacheSize) {
            cache.remove(cachePriority.get(0));
            cachePriority.remove(0);
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
        return entry.value;
    }
}
