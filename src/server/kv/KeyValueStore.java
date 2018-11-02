package server.kv;

import java.io.IOException;

public interface KeyValueStore {
    void init() throws IOException;

    /**
     *Returns the value associated with the {@code key} in the database
     * @param key the key for which the value is required
     * @return the value associated with the {@code key}
     * @throws KeyNotFoundException if the {@code key} is not found in the database
     * @throws DbError for other errors happened during the retrival of associated value from the database
     */
    String get(String key) throws KeyNotFoundException, DbError;

    /**
     *Puts the specified {@code key} and {@code value} in the database
     * @param key the key to be put in the database
     * @param value the value to be put in the database
     * @throws DbError any errors happened while writing the {@code key} and {@code value} to the database
     */
    void put(String key, String value) throws DbError;

    /**
     *Returns whether the specified {@code key} is present in the database
     * @param key the key
     * @return whether the key is present or not
     * @throws DbError any errors happened while checking the key is existing
     */
    boolean hasKey(String key) throws DbError;

    /**
     *Deletes the entry with specified {@code key} from the database
      * @param key the key which the entry is deleted
     * @return whether the delete of the entry was successful or not
     * @throws DbError any errors happened while deleting the record from the database
     */
    boolean deleteKey(String key) throws DbError;
}
