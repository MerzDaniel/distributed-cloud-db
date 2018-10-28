package server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.kv.CacheType;
import server.kv.KeyValueStore;
import server.kv.SimpleKeyValueStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server implements Runnable{
    final Logger logger = LogManager.getLogger(Server.class);
    final int port;
    private final int cacheSize;
    private final CacheType cacheType;

    private final KeyValueStore db = new SimpleKeyValueStore();

    public Server(int port) {
        this.port = port;
        cacheSize = 1000;
        cacheType = CacheType.FIFO;
    }

    public Server(int port, int cacheSize, CacheType cacheType) {
        this.port = port;
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
    }

    @Override
    public void run() {
        logger.info("Start server on port " + port);
        try {
            db.init();
        } catch (IOException e) {
            logger.error("Error while initializing database", e);
            System.exit(1);
        }
        ServerSocket s;
        try {
            s = new ServerSocket(port);
            while(true) {
                Socket clientSocket = s.accept();
                logger.debug("Accepted connection from client: " + clientSocket.getInetAddress());
                new Thread(new ConnectionHandler(clientSocket, db)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
