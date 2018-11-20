package server.kv.cache;

import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.kv.KeyValueStore;
import server.kv.SimpleKeyValueStore;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.stream.Stream;

/**
 * This class represent a KeyValueStore with an embedded caching mechanism. The
 * implementations of this class can decide upon the exact behaviour of the caching mechanism
 */
public abstract class CachedKeyValueStore implements KeyValueStore {

    private KeyValueStore store;
    int cacheSize;

    public CachedKeyValueStore(int cacheSize, String dataFileName) {
        this.cacheSize = cacheSize;
        store = new SimpleKeyValueStore(dataFileName);
    }

    public CachedKeyValueStore(int cacheSize, KeyValueStore store) {
        this.cacheSize = cacheSize;
        this.store = store;
    }

    @Override
    public void init() throws IOException {
        store.init();
    }

    /**
     *Returns the value associated with the {@code key} in the database
     *
     * @param key for which the value is required
     * @return the value associated with the {@code key}
     * @throws KeyNotFoundException if the {@code key} is not found in the database
     * @throws DbError if any other errors happened during the retrival of associated value from the database
     */
    @Override
    public String get(String key) throws KeyNotFoundException, DbError {
        if (isCached(key)) return getFromCache(key);

        String value = store.get(key);
        addToCache(key, value);
        return value;
    }

    /**
     *Puts the specified {@code key} and {@code value} in the database
     *
     * @param key the key to be put in the database
     * @param value the value to be put in the database
     * @throws DbError if any errors happened while writing the {@code key} and {@code value} to the database
     */
    @Override
    public boolean put(String key, String value) throws DbError {
        addToCache(key, value);
        return store.put(key, value);
    }

    /**
     *Returns whether the specified {@code key} is present in the database
     *
     * @param key the key
     * @return whether the key is present or not
     * @throws DbError if any any errors happened while checking the key is existing
     */
    @Override
    public boolean hasKey(String key) throws DbError {
        if (isCached(key)) return true;
        return store.hasKey(key);
    }

    /**
     *Deletes the entry with specified {@code key} from the database
     *
     * @param key the key which the entry is deleted
     * @return whether the delete of the entry was successful or not
     * @throws DbError if any errors happened while deleting the record from the database
     */
    @Override
    public boolean deleteKey(String key) throws DbError {
        removeFromCache(key);
        return store.deleteKey(key);
    }

    /**
     * shut downs the {@link KeyValueStore} by closing instances of any {@link java.io.Writer} or {@link java.io.Reader}
     *
     * @throws IOException if an error occured during the close of any {@link java.io.Writer} or {@link java.io.Reader}
     */
    @Override
    public void shutdown() throws IOException {
        store.shutdown();
    }

    @Override
    public Stream<AbstractMap.SimpleEntry<String, String>> retrieveAllData() {
        throw new NotImplementedException();
    }

    /**
     * add the given {@code key} and {@code value} into the database
     *
     * @param key the {@code key}
     * @param value the {@code value}
     */
    protected abstract void addToCache(String key, String value);

    /**
     * returns whether the {@code key} is present or not in the database
     *
     * @param key the {@code key} to be checked
     * @return whether the {@code key} is present or not
     */
    protected abstract boolean isCached(String key);

    /**
     * returns the value associated with the {@code key} in the database
     *
     * @param key the {@code key} for which the value is required
     * @return the value associated with the {@code key}
     * @throws KeyNotFoundException if any errors occurred while retrieving the value
     */
    protected abstract String getFromCache(String key) throws KeyNotFoundException;

    protected abstract void removeFromCache(String key);
}
