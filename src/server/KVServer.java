package server;

import lib.SocketUtil;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.kv.*;
import server.kv.cache.FifoCachedKeyValueStore;
import server.kv.cache.LFUCachedKeyValueStore;
import server.kv.cache.LRUCachedKeyValueStore;

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
    public KVServer(String host, int port) {
        RandomAccessKeyValueStore db = new RandomAccessKeyValueStore();
        serverData = new ServerData(host, port);
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

    public KVServer(String host, int port, int cacheSize, CacheType cacheType) {
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
        serverData = new ServerData(host, port);
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
    public KVServer(String host, int port, int cacheSize, CacheType cacheType, KeyValueStore db,
                    ServerState.State runningState) {
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
        serverData = new ServerData(host, port);
        state = new ServerState(db, serverData);
        state.runningState = runningState;
    }

    @Override
    public void run() {
        logger.info("Start server on port " + serverData.getPort());

        initDb();

        List<Socket> openConnections = new LinkedList<>();
        try (ServerSocket s = new ServerSocket(serverData.getPort())) {
            while (state.runningState != ServerState.State.SHUTTINGDOWN) {
                Socket clientSocket = s.accept();
                logger.debug("Accepted connection from client: " + clientSocket.getInetAddress());
                openConnections.add(clientSocket);
                new Thread(new ConnectionHandler(clientSocket, state)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        try {
            state.db.init();
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
        } catch (IOException e) {
            logger.error("Error while initializing database", e);
            System.exit(1);
        }
    }

    public void stop() throws IOException {
        state.runningState = ServerState.State.SHUTTINGDOWN;
        state.db.shutdown();
    }

}
