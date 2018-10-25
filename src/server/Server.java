package server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    final Logger logger = LogManager.getLogger(Server.class);
    final int port;
    private final int cacheSize;
    private final CacheType cacheType;

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
        ServerSocket s;
        try {
            s = new ServerSocket(port);
            while(true) {
                Socket clientSocket = s.accept();
                logger.debug("Accepted connection from client: " + clientSocket.getInetAddress());
                new Thread(new ConnectionHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
