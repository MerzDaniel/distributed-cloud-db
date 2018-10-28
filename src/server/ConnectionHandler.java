package server;

import lib.SocketUtil;
import lib.message.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static lib.SocketUtil.tryClose;

public class ConnectionHandler implements Runnable {
    final Socket s;
    final Logger logger = LogManager.getLogger(ConnectionHandler.class);

    public ConnectionHandler(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        OutputStream o = null;
        InputStream i = null;

        try {
            i = s.getInputStream();
            o = s.getOutputStream();

            while (SocketUtil.isConnected(s)) {
                String msg = SocketUtil.readMessage(i);
                try {
                    KVMessage kvMessage = null;
                    kvMessage = KVMessageUnmarshaller.unmarshall(msg);
                    logger.debug("Got a message: " + kvMessage.getStatus());

                    KVMessage response;
                    switch (kvMessage.getStatus()) {
                        case GET:
                            response = MessageFactory.createGetSuccessMessage(kvMessage.getKey(), "value");
                            break;
                        case PUT:
                            response = MessageFactory.createPutSuccessMessage();
                            break;
                        case DELETE:
                            response = MessageFactory.createDeleteSuccessMessage();
                            break;
                        default:
                            response = MessageFactory.createInvalidMessage();
                            break;
                    }

                    SocketUtil.sendMessage(o, KVMessageMarshaller.marshall(response));

                } catch (UnmarshallException e) {
                    logger.info("Got invalid message");
                    new KVMessageImpl(null, null, KVMessage.StatusType.INVALID_MESSAGE);
                }
            }
        } catch (IOException e) {
            logger.warn("Error during communication with an open connection:" + e.getMessage());
            logger.warn(e.getStackTrace());
        } finally {
            tryClose(o);
            tryClose(i);
            tryClose(s);
        }
    }
}

