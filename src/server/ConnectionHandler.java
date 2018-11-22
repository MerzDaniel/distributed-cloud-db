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
        try (InputStream i = s.getInputStream(); OutputStream o = s.getOutputStream()) {

            String connectMessage = MessageMarshaller.marshall(MessageFactory.creatConnectionSuccessful());
            SocketUtil.sendMessage(o, connectMessage);

            while (SocketUtil.isConnected(s) &&
                    state.runningState != RunningState.SHUTTINGDOWN) {
                IMessage response = handleIncomingMessage(i, o);
                SocketUtil.sendMessage(o, MessageMarshaller.marshall(response));
            }
        } catch (Exception e) {
            logger.warn("Error during communication with an open connection:" + e.getMessage(), e);
        } finally {
            tryClose(s);
        }
    }

    private IMessage handleIncomingMessage(InputStream i, OutputStream o) throws IOException, DbError {
        String msg = SocketUtil.readMessage(i);
        IMessage message;
        try {
            message = MessageMarshaller.unmarshall(msg);
        } catch (MarshallingException e) {
            logger.info("Got invalid message");
            return new KVMessageImpl(null, null, KVMessage.StatusType.INVALID_MESSAGE);
        }

        if (message instanceof KVMessage)
            return KvMessageHandler.handleKvMessage((KVMessage) message, state);

        return AdminMessageHandler.handleKvAdminMessage((KVAdminMessage) message, state);

    }

}

