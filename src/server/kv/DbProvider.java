package server.kv;

import lib.metadata.ServerData;
import lib.server.CacheType;
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
    /** If set to true dbs will be inMemory */
    private final boolean inMemory;
    private Logger logger = LogManager.getLogger(DbProvider.class);

    public DbProvider(ServerData coordinator) {
        inMemory = false;
        this.coordinator = coordinator;
        // initialize db of coordinator
        getDb(coordinator.getName());
    }

    /**
     * Only for TESTINg
     */
    public DbProvider(ServerData coordinator, KeyValueStore db, boolean inMemory) {
        this.coordinator = coordinator;
        this.inMemory = inMemory;
        dbMap.put(getDbName(coordinator.getName()), db);
    }

    /**
     * Get database for a specific ServerData
     */
    public KeyValueStore getDb(String serverName) {
        String dbName = getDbName(serverName);
        if (dbMap.get(dbName) != null) return dbMap.get(dbName);

        return createNewDb(serverName);
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

    private KeyValueStore createNewDb(String serverName) {
        KeyValueStore db = inMemory ? new MemoryDatabase() : new RandomAccessKeyValueStore();
        String dbName = getDbName(serverName);
        try {
            db.init(dbName);
        } catch (IOException e) {
            logger.warn("Could not create database");
            return null;
        }

        CacheType cacheType = coordinator.getCacheType();
        switch (cacheType) {
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

        dbMap.put(dbName, db);
        return db;
    }

    private String getDbName(String serverName) {
        return this.isCoordiantor(serverName) ?
                serverName :
                "replica_" + serverName + "_within_" + coordinator.getName();
    }

    private boolean isCoordiantor(String serverName) {
        return serverName.equals(this.coordinator.getName()) ? true : false;

    }
}
