package server.kv;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    public LRUCachedKeyValueStore(int cacheSize) {
        super(cacheSize);
    }

    public LRUCachedKeyValueStore(int cacheSize, KeyValueStore store) {
        super(cacheSize, store);
    }

    @Override
    protected void addToCache(String key, String value) {
        if (isCached(key)) return;
        if (cache.size() >= this.cacheSize) {
            String leastRecentlyUsedKey = cachePriority.get(this.cacheSize - 1);
            cachePriority.remove(leastRecentlyUsedKey);
            cache.remove(leastRecentlyUsedKey);
        }
        cachePriority.add(key);
        cache.put(key, new CacheEntry(key, value));
    }

    @Override
    protected boolean isCached(String key) {
        return cache.get(key) == null;
    }

    @Override
    protected String getFromCache(String key) throws KeyNotFoundException {
        CacheEntry entry = cache.get(key);
        if (entry == null) throw new KeyNotFoundException();
        //make this key the most recently used by moving it to the top
        cachePriority.remove(key);
        cachePriority.add(key);
        return entry.value;
    }
}
