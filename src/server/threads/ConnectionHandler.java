package server.threads;

import lib.message.*;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;
import server.kv.DbError;
import server.threads.handler.AdminMessageHandler;
import server.threads.handler.KvMessageHandler;

import java.io.IOException;
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
        Messaging messaging = new Messaging();
        try {
            messaging.connect(s);
            messaging.sendMessage(MessageFactory.creatConnectionSuccessful());
        } catch (Exception e) {
            logger.warn("Error setting up new connection");
            tryClose(s);
            return;
        }


        try {
            while (messaging.isConnected() &&
                    !shouldStop) {

                try {
                    IMessage request = null;
                    request = messaging.readMessageWithoutTimeout();
                    IMessage response;
                    if (request instanceof KVMessage)
                        response = KvMessageHandler.handleKvMessage((KVMessage) request, state);
                    else
                        response = AdminMessageHandler.handleKvAdminMessage((KVAdminMessage) request, state);

                    messaging.sendMessage(response);
                } catch (Exception e) {
                    logger.warn("Error occured!", e);
                }
            }
        } finally {
            tryClose(s);
        }
//        } catch (Exception e) {
//            logger.debug("Error during communication with an open connection:" + e.getMessage(), e);
//        } finally {
//            tryClose(s);
//        }
    }
}

