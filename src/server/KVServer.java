package server;

import lib.SocketUtil;
import lib.metadata.MetaContent;
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
    private boolean stopRequested = false;
    private final ServerState state;
    private final MetaContent metaContent;

    /**
     * Start KV Server at given port
     *
     * @param port given port for storage server to operate
     */
    public KVServer(String host, int port) {
        RandomAccessKeyValueStore db = new RandomAccessKeyValueStore();
        metaContent = new MetaContent(host, port);
        state = new ServerState(db, metaContent);
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
        metaContent = new MetaContent(host, port);
        RandomAccessKeyValueStore db = new RandomAccessKeyValueStore();
        state = new ServerState(db, metaContent);
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
    public KVServer(String host, int port, int cacheSize, CacheType cacheType, KeyValueStore db) {
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
        metaContent = new MetaContent(host, port);
        state = new ServerState(db, metaContent);
    }

    @Override
    public void run() {
        logger.info("Start server on port " + metaContent.getPort());

        initDb();

        List<Socket> openConnections = new LinkedList<>();
        try (ServerSocket s = new ServerSocket(metaContent.getPort())) {
            while (!stopRequested) {
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
        stopRequested = true;
        state.db.shutdown();
    }

}
