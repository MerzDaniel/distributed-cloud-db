package server.kv;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public abstract class CachedKeyValueStore implements KeyValueStore {

    private KeyValueStore store;
    private int cacheSize;

    public CachedKeyValueStore(int cacheSize) {
        this.cacheSize = cacheSize;
        store = new SimpleKeyValueStore();
    }
    public CachedKeyValueStore(int cacheSize, KeyValueStore store) {
        this.cacheSize = cacheSize;
        this.store = store;
    }

    @Override
    public void init() throws IOException {
        store.init();
    }

    @Override
    public String get(String key) throws KeyNotFoundException, DbError {
        return store.get(key);
    }

    @Override
    public void put(String key, String value) throws DbError {
        store.put(key, value);
    }

    @Override
    public boolean hasKey(String key) throws DbError {
        return store.hasKey(key);
    }

    @Override
    public boolean deleteKey() throws DbError {
        return store.deleteKey();
    }

    protected abstract void addToCache(String key, String value);
    protected abstract boolean isCached(String key);
    protected abstract String getFromCache(String key);
}
