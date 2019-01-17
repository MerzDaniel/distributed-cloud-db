package server.kv;

import lib.Json;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.stream.Stream;

public class GraphDatabase extends KeyValueStore{
    private KeyValueStore store = new MemoryDatabase();

    @Override
    public void init(String dbName) throws IOException {

    }

    @Override
    public void shutdown() throws IOException {

    }

    @Override
    public String get(String key) throws KeyNotFoundException, DbError {
        Json query = Json.deserialize(key);
        return null;
    }

    @Override
    public boolean put(String key, String value) throws DbError {
        String irelevantKey = key;
        Json mutationParameters = Json.deserialize(value);

        /*
        // definition for mutations which have to applied to documents
//      mutationParameters =  {
//                <objIdA>: {
                        <propteryA>: val   // sets the property
                        <propertyB>: [val] // appends "val" to list
                        <propteryC>: null  // unsets the property
//
//                  }
//                <objIdB>: {
                        <propteryA>: val   // sets the property
                        <propertyB>: [val] // appends "val" to list
                        <propteryC>: null  // unsets the property
//                  }
//         }
*/

        return false;
    }

    @Override
    public boolean hasKey(String key) throws DbError {
        return false;
    }

    @Override
    public boolean deleteKey(String key) throws DbError {
        return false;
    }

    @Override
    public Stream<AbstractMap.SimpleEntry<String, String>> retrieveAllData() {
        return null;
    }
}
