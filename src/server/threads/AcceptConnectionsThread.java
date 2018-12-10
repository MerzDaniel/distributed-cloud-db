package server.threads;

import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class AcceptConnectionsThread extends AbstractServerThread {
    private Logger logger = LogManager.getLogger(AcceptConnectionsThread.class);
    private ServerSocket s;
    private List<Socket> openConnections;
    private ServerState state;

    public AcceptConnectionsThread(ServerSocket s, List<Socket> openConnections, ServerState state) {
        this.s = s;
        this.openConnections = openConnections;
        this.state = state;
    }

    @Override
    public void run() {
        while (!shouldStop) {
            Socket clientSocket;
            try {
                clientSocket = s.accept();
            } catch (IOException e) {
                logger.info("error on client connection");
                continue;
            }
            logger.debug("Accepted connection from client: " + clientSocket.getInetAddress());
            openConnections.add(clientSocket);
            ConnectionHandler ch = new ConnectionHandler(clientSocket, state);
            ch.start();
            state.serverThreads.add(ch);
        }
    }
}
