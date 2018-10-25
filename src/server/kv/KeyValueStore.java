package server.kv;

public interface KeyValueStore {
    String get(String key) throws KeyNotFoundException, DbError;

    void put(String key, String value) throws DbError;

    boolean hasKey(String key) throws DbError;

    boolean deleteKey() throws DbError;
}
