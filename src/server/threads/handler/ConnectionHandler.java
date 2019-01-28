package server.threads.handler;

import lib.TimeWatch;
import lib.message.IMessage;
import lib.message.Messaging;
import lib.message.admin.KVAdminMessage;
import lib.message.graph.GraphDbMessage;
import lib.message.kv.KVMessage;
import lib.message.kv.KvMessageFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;
import server.threads.AbstractServerThread;
import server.threads.handler.admin.AdminMessageHandler;
import server.threads.handler.graph.GraphMessageHandler;
import server.threads.handler.kv.KvMessageHandler;

import java.net.Socket;
import java.util.concurrent.TimeUnit;

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
            messaging.sendMessage(KvMessageFactory.creatConnectionSuccessful());
        } catch (Exception e) {
            logger.warn("Error setting up new connection");
            messaging.disconnect();
            tryClose(s);
            return;
        }

        try {
            while (messaging.isConnected() &&
                    !shouldStop) {

                boolean gotRequest = false;
                try {
                    IMessage request = messaging.readMessageWithoutTimeout();
                    TimeWatch t = TimeWatch.start();
                    logger.debug("REQUEST: " + request.marshall());
                    gotRequest = true;
                    IMessage response = handleRequest(request);

                    logger.debug(String.format("RESPONSE (%d ms): %s", t.time(TimeUnit.MILLISECONDS) , response.marshall()));
                    messaging.sendMessage(response);
                    continue;
                } catch (Exception e) {
                    logger.info("Error occured!", e);
                }

                if (gotRequest) {
                    try {
                        messaging.sendMessage(KvMessageFactory.createServerError());
                    } catch (Exception e1) {
                        logger.info("Error occured!", e1);
                    }
                }
                return;
            }
        } finally {
            tryClose(s);
        }
    }

    private IMessage handleRequest(IMessage request) throws Exception {
        IMessage response;
        if (request instanceof KVMessage)
            response = KvMessageHandler.handleKvMessage((KVMessage) request, state);
        else if (request instanceof KVAdminMessage)
            response = AdminMessageHandler.handleKvAdminMessage((KVAdminMessage) request, state);
        else if (request instanceof GraphDbMessage)
            response = GraphMessageHandler.handle((GraphDbMessage) request, state);
        else
            throw new Exception("Unknown Msg");
        return response;
    }
}

