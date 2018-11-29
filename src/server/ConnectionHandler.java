package server;

import lib.SocketUtil;
import lib.message.*;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.handler.AdminMessageHandler;
import server.handler.KvMessageHandler;
import server.kv.DbError;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static lib.SocketUtil.tryClose;

/**
 * This class handles a single client request in a separate thread
 */
public class ConnectionHandler implements Runnable {
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
                    state.runningState != RunningState.SHUTTINGDOWN) {
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
    }
}

