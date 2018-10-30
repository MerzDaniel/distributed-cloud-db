package server.kv;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LFUCachedKeyValueStore extends CachedKeyValueStore {
    class CacheEntry {
        public CacheEntry(String key, String value, int hitCount) {
            this.key = key;
            this.value = value;
            this.hitCount = hitCount;
        }

        public String key;
        public String value;
        public int hitCount;
    }

    Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public LFUCachedKeyValueStore(int cacheSize) {
        super(cacheSize);
    }

    public LFUCachedKeyValueStore(int cacheSize, KeyValueStore store) {
        super(cacheSize, store);
    }

    @Override
    protected void addToCache(String key, String value) {
        if (isCached(key)) return;
        if (cache.size() >= this.cacheSize) {
            Optional<CacheEntry> leastFrequentlyUsedKey = cache.values().stream().min(new Comparator<CacheEntry>() {
                @Override
                public int compare(CacheEntry o1, CacheEntry o2) {
                    return Integer.valueOf(o1.hitCount).compareTo(o2.hitCount);
                }
            });

            if (leastFrequentlyUsedKey.get() != null) {
                cache.remove(leastFrequentlyUsedKey.get().key);
            }
            cache.remove(leastFrequentlyUsedKey);
        }

        cache.put(key, new CacheEntry(key, value, 1));
    }

    @Override
    protected boolean isCached(String key) {
        return cache.get(key) != null;
    }

    @Override
    protected String getFromCache(String key) throws KeyNotFoundException {
        CacheEntry entry = cache.get(key);
        if (entry == null) throw new KeyNotFoundException();
        entry.hitCount++;
        return entry.value;
    }
}
