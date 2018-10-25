package server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    final Logger logger = LogManager.getLogger(Server.class);
    final int port;

    public Server(int port) {
        this.port = port;
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

    public static void main(String[] args) {
        int port;
        if (args.length >0) port = Integer.parseInt(args[0]);
        else port = 50000;

        new Server(port).run();
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2-server.properties.xml");
    }
}
