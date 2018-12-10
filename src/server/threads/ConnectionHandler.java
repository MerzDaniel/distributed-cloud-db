package server.threads;

import lib.message.*;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;
import server.threads.handler.AdminMessageHandler;
import server.threads.handler.KvMessageHandler;

import java.net.Socket;

import static lib.SocketUtil.tryClose;

/**
 * This class handles a single client request in a separate thread
 */
public class ConnectionHandler extends AbstractServerThread {
    final Socket s;
    private ServerState state;
    final Logger logger = LogManager.getLogger(ConnectionHandler.class);

    public ConnectionHandler(Socket s, ServerState state) {
        this.s = s;
        this.state = state;
    }

    /**
     * start the ConnectionHandler thread
     */
    @Override
    public void run() {
        try {
            Messaging messaging = new Messaging();
            messaging.connect(s);

            messaging.sendMessage(MessageFactory.creatConnectionSuccessful());

            while (messaging.isConnected() &&
                    !shouldStop) {
                IMessage request = messaging.readMessage();

                IMessage response;
                if (request instanceof KVMessage)
                    response = KvMessageHandler.handleKvMessage((KVMessage) request, state);
                else
                    response = AdminMessageHandler.handleKvAdminMessage((KVAdminMessage) request, state);

                messaging.sendMessage(response);
            }
        } catch (Exception e) {
            logger.warn("Error during communication with an open connection:" + e.getMessage(), e);
        } finally {
            tryClose(s);
        }
        state.serverThreads.remove(this);
    }
}

