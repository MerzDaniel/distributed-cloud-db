package server.kv;

import client.store.KVStore;
import lib.metadata.ServerData;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;

public class DbProvider {
//    HashMap<String, KeyValueStore> dbMap;

    public KeyValueStore getDb(String dbName, ServerData coordinator) {
        throw new NotImplementedException();
        // does not exist
//        new DB().init()
    }

    public void shutdown() {

    }
}
