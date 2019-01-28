package server.threads.handler;

import lib.TimeWatch;
import lib.message.IMessage;
import lib.message.Messaging;
import lib.message.admin.KVAdminMessage;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;
import lib.message.graph.query.QueryMessageImpl;
import lib.message.kv.KVMessage;
import lib.message.kv.KvMessageFactory;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;
import server.threads.AbstractServerThread;
import server.threads.handler.admin.AdminMessageHandler;
import server.threads.handler.graph.GraphMessageHandler;
import server.threads.handler.kv.KvMessageHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
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

                IMessage request;
                try {
                    request = messaging.readMessageWithoutTimeout();
                } catch (IOException e) {
                    return;
                }
                TimeWatch t = TimeWatch.start();
                try {
                    logger.debug("REQUEST: " + request.marshall());
                } catch (MarshallingException e) {
                }
                IMessage response;
                try {
                    response = handleRequest(request);
                } catch (Exception e) {
                    response = KvMessageFactory.createServerError();
                    logger.warn("SERVVER ERROR", e);
                }

                logger.debug(String.format("RESPONSE (%d ms): %s", t.time(TimeUnit.MILLISECONDS), response.marshall()));
                messaging.sendMessage(response);
                continue;
            }
        } catch (MarshallingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tryClose(s);
        }
    }

    private IMessage handleRequest(IMessage request) throws Exception {
        if (request instanceof KVAdminMessage)
            return AdminMessageHandler.handleKvAdminMessage((KVAdminMessage) request, state);

        IMessage msgNotAllowedResponse = createMessageNotAllowedResponse(request);
        if (msgNotAllowedResponse != null) return msgNotAllowedResponse;

        if (request instanceof KVMessage)
            return KvMessageHandler.handleKvMessage((KVMessage) request, state);
        if (request instanceof GraphDbMessage)
            return GraphMessageHandler.handle((GraphDbMessage) request, state);

        throw new Exception("Unknown Msg");
    }

    private IMessage createMessageNotAllowedResponse(IMessage request) {
        if (
                !Arrays.asList(RunningState.RUNNING, RunningState.READONLY).contains(state.runningState)) {
            logger.info(String.format("Client issued following msg while server is in state %s: %s",
                    state.runningState.toString(), request.prettyPrint())
            );
            return KvMessageFactory.creatServerStopped();
        }

        if (state.runningState == RunningState.READONLY && !(isReadingMsg(request))) {
            logger.info(String.format("Client issued msg while server is in state %s: %s",
                    state.runningState.toString(), request.prettyPrint())
            );
            return KvMessageFactory.createServerWriteLock();
        }

        return null;
    }

    private boolean isReadingMsg(IMessage request) {
        if (request instanceof KVMessage)
            return ((KVMessage) request).getStatus().equals(KVMessage.StatusType.GET);

        return request instanceof QueryMessageImpl;

    }
}

