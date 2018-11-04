package server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.kv.*;
import server.kv.cache.FifoCachedKeyValueStore;
import server.kv.cache.LFUCachedKeyValueStore;
import server.kv.cache.LRUCachedKeyValueStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class represents the Server instances
 * A Server is distinguished from it's port. So different servers run on different ports
 */
public class Server implements Runnable {
    final Logger logger = LogManager.getLogger(Server.class);
    final int port;
    private int cacheSize = 10;
    private CacheType cacheType = CacheType.NONE;
    private boolean stopRequested = false;
    private KeyValueStore db;

    public Server(int port) {
        this.port = port;
        db = new RandomAccessKeyValueStore();
    }

    public Server(int port, int cacheSize, CacheType cacheType) {
        this.port = port;
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
        db = new RandomAccessKeyValueStore();
    }

    public Server(int port, int cacheSize, CacheType cacheType, KeyValueStore db) {
        this.port = port;
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
        this.db = db;
    }

    @Override
    public void run() {
        logger.info("Start server on port " + port);

        initDb();

        try {
            ServerSocket s;
            s = new ServerSocket(port);
            while (!stopRequested) {
                Socket clientSocket = s.accept();
                logger.debug("Accepted connection from client: " + clientSocket.getInetAddress());
                new Thread(new ConnectionHandler(clientSocket, db)).start();
            }
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
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
