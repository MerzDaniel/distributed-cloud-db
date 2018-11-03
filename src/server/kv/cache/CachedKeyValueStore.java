package server.kv.cache;

import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.kv.KeyValueStore;
import server.kv.SimpleKeyValueStore;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import java.io.IOException;

public abstract class CachedKeyValueStore implements KeyValueStore {

    private KeyValueStore store;
    int cacheSize;

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
        if (isCached(key)) return getFromCache(key);

        String value = store.get(key);
        addToCache(key, value);
        return value;
    }

    @Override
    public boolean put(String key, String value) throws DbError {
        return store.put(key, value);
    }

    @Override
    public boolean hasKey(String key) throws DbError {
        if (isCached(key)) return true;
        return store.hasKey(key);
    }

    @Override
    public boolean deleteKey(String key) throws DbError {
        throw new NotImplementedException();
    }

    @Override
    public void shutdown() throws IOException {
        store.shutdown();
    }

    protected abstract void addToCache(String key, String value);
    protected abstract boolean isCached(String key);
    protected abstract String getFromCache(String key) throws KeyNotFoundException;
}
