package server;

import lib.SocketUtil;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.CacheType;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.kv.*;
import server.kv.cache.FifoCachedKeyValueStore;
import server.kv.cache.LFUCachedKeyValueStore;
import server.kv.cache.LRUCachedKeyValueStore;
import server.threads.AcceptConnectionsThread;
import server.threads.ConnectionHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the KVServer instances
 * A KVServer is distinguished from it's port. So different servers run on different ports
 */
public class KVServer implements Runnable {
    final Logger logger = LogManager.getLogger(KVServer.class);
    private int cacheSize = 10;
    private CacheType cacheType = CacheType.NONE;
    private final ServerState state;
    private final ServerData serverData;

    /**
     * Start KV Server at given port
     *
     * @param port given port for storage server to operate
     */
    public KVServer(String name, String host, int port) {
        RandomAccessKeyValueStore db = new RandomAccessKeyValueStore();
        serverData = new ServerData(name, host, port);
        state = new ServerState(db, serverData);
    }

    /**
     * Start KV Server at given port
     *
     * @param port      given port for storage server to operate
     * @param cacheSize specifies how many key-value pairs the server is allowed
     *                  to keep in-memory
     * @param cacheType specifies the cache replacement strategy in case the cache
     *                  is full and there is a GET- or PUT-request on a key that is
     *                  currently not contained in the cache. Options are "FIFO", "LRU",
     *                  and "LFU".
     */

    public KVServer(String name, String host, int port, int cacheSize, CacheType cacheType) {
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
        serverData = new ServerData(name, host, port);
        RandomAccessKeyValueStore db = new RandomAccessKeyValueStore();
        state = new ServerState(db, serverData);
    }

    /**
     * Start KV Server at given port
     *
     * @param port      given port for storage server to operate
     * @param cacheSize specifies how many key-value pairs the server is allowed
     *                  to keep in-memory
     * @param cacheType specifies the cache replacement strategy in case the cache
     *                  is full and there is a GET- or PUT-request on a key that is
     *                  currently not contained in the cache. Options are "FIFO", "LRU",
     *                  and "LFU".
     * @param db        the {@link KeyValueStore} associated with the KVServer instance
     */
    public KVServer(String name, String host, int port, int cacheSize, CacheType cacheType, KeyValueStore db,
                    RunningState runningState) {
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
        serverData = new ServerData(name, host, port);
        state = new ServerState(db, serverData);
        state.runningState = runningState;
        state.meta = new KVStoreMetaData();
        state.meta.getKvServerList().add(serverData);
    }

    List<Socket> openConnections = new LinkedList<>();

    @Override
    public void run() {
        logger.info("Start server on port " + serverData.getPort());

        initDb();

        try (ServerSocket s = new ServerSocket(serverData.getPort())) {

            Thread acceptThread = new AcceptConnectionsThread(s, openConnections, state);
            acceptThread.start();

            // Server loop, wait for getting shutdown
            while(state.runningState != RunningState.SHUTTINGDOWN) {
                Thread.sleep(500);
            }

            acceptThread.stop();

        } catch (IOException e) {
            logger.warn("IO error on creating Socket", e);
        } catch (InterruptedException e) {
            logger.warn("Thread aborted", e);
        } finally {
            for (Socket s : openConnections) {
                SocketUtil.tryClose(s);
            }
            try {
                state.db.shutdown();
            } catch (IOException e) {
                logger.warn("Problem during shutting down db", e);
            }
        }
    }

    private void initDb() {
        switch (cacheType) {
            case FIFO:
                logger.info("Setting up FIFO caching");
                state.db = new FifoCachedKeyValueStore(cacheSize, state.db);
                break;
            case LRU:
                logger.info("Setting up LRU caching");
                state.db = new LRUCachedKeyValueStore(cacheSize, state.db);
                break;
            case LFU:
                logger.info("Setting up LFU caching");
                state.db = new LFUCachedKeyValueStore(cacheSize, state.db);
                break;
        }
    }

    public void stop() throws IOException {
        state.runningState = RunningState.SHUTTINGDOWN;
        state.db.shutdown();
    }

}
