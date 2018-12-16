package server.kv;

import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.kv.cache.FifoCachedKeyValueStore;
import server.kv.cache.LFUCachedKeyValueStore;
import server.kv.cache.LRUCachedKeyValueStore;

import java.io.IOException;
import java.util.HashMap;

public class DbProvider {
    HashMap<String, KeyValueStore> dbMap = new HashMap<>();
    private ServerData coordinator;
    private Logger logger = LogManager.getLogger(DbProvider.class);

    public DbProvider(ServerData coordinator) {
        this.coordinator = coordinator;
        // initialize db of coordinator
        getDb(coordinator);
    }

    /**
     * Only for TESTINg
     */
    public DbProvider(ServerData coordinator, KeyValueStore db) {
        this.coordinator = coordinator;
        dbMap.put(coordinator.getName(), db);
    }

    /**
     * Get database for a specific ServerData
     */
    public KeyValueStore getDb(ServerData server) {
        String serverName = server.getName();
        if (dbMap.get(serverName) != null) return dbMap.get(serverName);

        return createNewDb(server);
    }

    public void shutdown() {
        dbMap.values().forEach(d -> {
            try {
                d.shutdown();
            } catch (IOException e) {
                logger.error("Problem while shutting down the database");
            }
        });
    }

    private KeyValueStore createNewDb(ServerData server) {
        KeyValueStore db = new RandomAccessKeyValueStore();
        try {
            db.init(server.getName());
        } catch (IOException e) {
            logger.warn("Could not create database");
            return null;
        }
        switch (coordinator.getCacheType()) {
            case FIFO:
                logger.info("Setting up FIFO caching");
                db = new FifoCachedKeyValueStore(coordinator.getCacheSize(), db);
                break;
            case LRU:
                logger.info("Setting up LRU caching");
                db = new LRUCachedKeyValueStore(coordinator.getCacheSize(), db);
                break;
            case LFU:
                logger.info("Setting up LFU caching");
                db = new LFUCachedKeyValueStore(coordinator.getCacheSize(), db);
                break;
        }

        dbMap.put(server.getName(), db);
        return db;
    }
}
