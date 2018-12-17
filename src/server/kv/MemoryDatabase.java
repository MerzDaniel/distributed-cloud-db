package server.kv;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MemoryDatabase implements KeyValueStore {
    ConcurrentHashMap<String, String> db = new ConcurrentHashMap<>();

    @Override
    public void init(String dbName) throws IOException {
        db.clear();
    }

    @Override
    public void shutdown() throws IOException {
        db.clear();
    }

    @Override
    public String get(String key) throws KeyNotFoundException, DbError {
        return db.get(key);
    }

    @Override
    public boolean put(String key, String value) throws DbError {
        return db.put(key, value) != null;
    }

    @Override
    public boolean hasKey(String key) throws DbError {
        return db.get(key) != null;
    }

    @Override
    public boolean deleteKey(String key) throws DbError {
        return db.remove(key) != null;
    }

    @Override
    public Stream<AbstractMap.SimpleEntry<String, String>> retrieveAllData() {
        return db.entrySet().stream().map(e ->
                new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue())
        );
    }
}
