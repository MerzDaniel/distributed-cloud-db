package server;

import lib.SocketUtil;
import lib.message.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.handler.GetHandler;
import server.handler.PutHandler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import static lib.SocketUtil.tryClose;
import static lib.message.MessageUtil.isValidKey;
import static lib.message.MessageUtil.isValidValue;

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

            while (SocketUtil.isConnected(s)) {
                KVMessage response = handleIncomingMessage(i, o);
                SocketUtil.sendMessage(o, MessageMarshaller.marshall(response));
            }
        } catch (Exception e) {
            logger.warn("Error during communication with an open connection:" + e.getMessage(), e);
        } finally {
            tryClose(s);
        }
    }

    private KVMessage handleIncomingMessage(InputStream i, OutputStream o) throws IOException {
        String msg = SocketUtil.readMessage(i);
        IMessage message;
        try {
            message = MessageMarshaller.unmarshall(msg);
        } catch (MarshallingException e) {
            logger.info("Got invalid message");
            return new KVMessageImpl(null, null, KVMessage.StatusType.INVALID_MESSAGE);
        }

        if (message instanceof KVMessage)
            return handleKvMessage((KVMessage) message);

        return handleKvAdminMessage((KVAdminMessage) message);

    }

    private KVMessage handleKvAdminMessage(KVAdminMessage message) {

        // handle Admin messages
        throw new NotImplementedException();
    }

    private KVMessage handleKvMessage(KVMessage kvMessage) {
        if (
                !Arrays.asList(ServerState.State.RUNNING, ServerState.State.READONLY).contains(state.runningState)) {
            logger.info(String.format("Client issued %s while server is in state %s",
                    kvMessage.getStatus().toString(), state.runningState.toString())
            );
            return MessageFactory.creatServerStopped();
        }

        // todo: check responsible

        if (state.runningState == ServerState.State.READONLY && kvMessage.getStatus() != KVMessage.StatusType.GET) {
            logger.info(String.format("Client issued %s while server is in state %s",
                    kvMessage.getStatus().toString(), state.runningState.toString())
            );
            return MessageFactory.createServerWriteLock();
        }

        logger.debug(String.format(
                "Got a message: %s <%s,%s>",
                kvMessage.getStatus(), kvMessage.getKey(), kvMessage.getValue()
        ));

        if (!isValidKeyValueLength(kvMessage)) {
            logger.info(String.format("Key or Value are too long. Only a size for key/value of 20/120kb is allowed. key=%S | value=%s", kvMessage.getKey(), kvMessage.getValue()));
            return MessageFactory.createInvalidMessage();
        }

        switch (kvMessage.getStatus()) {
            case GET:
                return new GetHandler().handleRequest(kvMessage, state);
            case PUT:
                logger.debug(String.format("New PUT message from client: <%s,%s>", kvMessage.getKey(), kvMessage.getValue()));
                return new PutHandler().handleRequest(kvMessage, state);
            case DELETE:
                return MessageFactory.createDeleteErrorMessage();
            default:
                return MessageFactory.createInvalidMessage();
        }
    }

    private boolean isValidKeyValueLength(KVMessage message) {
        switch (message.getStatus()) {
            case GET:
                return isValidKey(message.getKey());
            case PUT:
                return isValidKey(message.getKey()) && isValidValue(message.getValue());
            case DELETE:
                return isValidKey(message.getKey());
        }

        return true;
    }
}

