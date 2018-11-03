package server.kv.cache;

import server.kv.KeyNotFoundException;
import server.kv.KeyValueStore;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

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

    @Override
    protected void addToCache(String key, String value) {
        if (isCached(key)) return;
        if (cache.size() >= this.cacheSize) {
            cache.remove(cachePriority.get(0));
            cachePriority.remove(0);
        }
        cachePriority.add(key);
        cache.put(key, new CacheEntry(key, value));
    }

    @Override
    protected boolean isCached(String key) {
        return cache.get(key) != null;
    }

    @Override
    protected String getFromCache(String key) throws KeyNotFoundException {
        CacheEntry entry = cache.get(key);
        if (entry == null) throw new KeyNotFoundException();
        return entry.value;
    }
}
