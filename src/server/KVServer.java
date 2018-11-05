package server;

import lib.SocketUtil;
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
    final int port;
    private int cacheSize = 10;
    private CacheType cacheType = CacheType.NONE;
    private boolean stopRequested = false;
    private KeyValueStore db;

    /**
     * Start KV Server at given port
     *
     * @param port      given port for storage server to operate
     */
    public KVServer(int port) {
        this.port = port;
        db = new RandomAccessKeyValueStore();
    }

    /**
     * Start KV Server at given port
     *
     * @param port      given port for storage server to operate
     * @param cacheSize specifies how many key-value pairs the server is allowed
     *                  to keep in-memory
     * @param cacheType  specifies the cache replacement strategy in case the cache
     *                  is full and there is a GET- or PUT-request on a key that is
     *                  currently not contained in the cache. Options are "FIFO", "LRU",
     *                  and "LFU".
     */

    public KVServer(int port, int cacheSize, CacheType cacheType) {
        this.port = port;
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
        db = new RandomAccessKeyValueStore();
    }

    /**
     * Start KV Server at given port
     *
     * @param port      given port for storage server to operate
     * @param cacheSize specifies how many key-value pairs the server is allowed
     *                  to keep in-memory
     * @param cacheType  specifies the cache replacement strategy in case the cache
     *                  is full and there is a GET- or PUT-request on a key that is
     *                  currently not contained in the cache. Options are "FIFO", "LRU",
     *                  and "LFU".
     * @param db the {@link KeyValueStore} associated with the KVServer instance
     */
    public KVServer(int port, int cacheSize, CacheType cacheType, KeyValueStore db) {
        this.port = port;
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
        this.db = db;
    }

    @Override
    public void run() {
        logger.info("Start server on port " + port);

        initDb();

        List<Socket> openConnections = new LinkedList<>();
        try (ServerSocket s = new ServerSocket(port)) {
            while (!stopRequested) {
                Socket clientSocket = s.accept();
                logger.debug("Accepted connection from client: " + clientSocket.getInetAddress());
                openConnections.add(clientSocket);
                new Thread(new ConnectionHandler(clientSocket, db)).start();
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
            db.init();
            switch (cacheType) {
                case FIFO:
                    logger.info("Setting up FIFO caching");
                    db = new FifoCachedKeyValueStore(cacheSize, db);
                    break;
                case LRU:
                    logger.info("Setting up LRU caching");
                    db = new LRUCachedKeyValueStore(cacheSize, db);
                    break;
                case LFU:
                    logger.info("Setting up LFU caching");
                    db = new LFUCachedKeyValueStore(cacheSize, db);
                    break;
            }
        } catch (IOException e) {
            logger.error("Error while initializing database", e);
            System.exit(1);
        }
    }

    public void stop() throws IOException {
        stopRequested = true;
        db.shutdown();
    }

}
