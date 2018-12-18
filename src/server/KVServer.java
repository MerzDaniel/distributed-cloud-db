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
    private final ServerData intermediateServerData;

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
        // create intermediate serverData till this server gets configured
        intermediateServerData = new ServerData(name, host, port);
        state = new ServerState(intermediateServerData);
    }

    /**
     * Startup the server for TESTING
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
        intermediateServerData = new ServerData(name, host, port);
        state = new ServerState(db, intermediateServerData);
        state.runningState = runningState;
        state.meta = new KVStoreMetaData();
        state.meta.getKvServerList().add(intermediateServerData);
    }

    /**
     * For tests
     * @param serverData
     */
    public KVServer(ServerData serverData, KeyValueStore db) {
        intermediateServerData = serverData;
        state = new ServerState(db, intermediateServerData);
    }

    List<Socket> openConnections = new LinkedList<>();

    @Override
    public void run() {
        logger.info("Start server on port " + intermediateServerData.getPort());

        try (ServerSocket s = new ServerSocket(intermediateServerData.getPort())) {

            AcceptConnectionsThread acceptThread = new AcceptConnectionsThread(s, openConnections, state);
            state.serverThreads.add(acceptThread);
            acceptThread.start();

            // Server loop, wait for getting shutdown
            while (state.runningState != RunningState.SHUTTINGDOWN) {
                Thread.sleep(500);
            }

            stop();

        } catch (IOException e) {
            logger.warn("IO error on creating Socket", e);
        } catch (InterruptedException e) {
            logger.warn("Thread aborted", e);
        } finally {
            try {
                stop();
            } catch (IOException e) {
                logger.warn("Error on shutdown", e);
            }
        }
    }

    public void stop() throws IOException {
        for (Socket s : openConnections) {
            SocketUtil.tryClose(s);
        }
        state.serverThreads.forEach(st -> {
            if(st!=null) st.stopServerThread();
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.warn("interrupted");
        }
        state.serverThreads.forEach(st -> {
            if(st!=null) st.stop();
        });
        state.runningState = RunningState.SHUTTINGDOWN;
        state.dbProvider.shutdown();
    }

    /** Only use in Tests! */
    public ServerState getState() {
        return state;
    }

    private void tryStopThread(Thread t) {
        t.stop();
    }

}
