package server.kv;

import java.io.IOException;

public interface KeyValueStore {
    void init() throws IOException;
    String get(String key) throws KeyNotFoundException, DbError;

    void put(String key, String value) throws DbError;

    boolean hasKey(String key) throws DbError;

    boolean deleteKey(String key) throws DbError;
}
