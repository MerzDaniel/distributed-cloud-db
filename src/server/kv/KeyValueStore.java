package server.kv;

public interface KeyValueStore {
    String get(String key) throws KeyNotFoundException, DbError;
    void put(String key);
}
