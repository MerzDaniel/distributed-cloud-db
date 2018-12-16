package server.kv;

import lib.metadata.ServerData;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;

public class DbProvider {
    HashMap<String, KeyValueStore> dbMap = new HashMap<>();


    public DbProvider(ServerData coordinator) {

    }

    /**
     * Get database which handles a specific dataKey
     */
    public KeyValueStore getDb(String dataKey) {
        throw new NotImplementedException();
    }

    /**
     * Get database for a specific ServerData
     */
    public KeyValueStore getDb(ServerData server) {
        throw new NotImplementedException();
    }

    public void shutdown() {

    }
}
