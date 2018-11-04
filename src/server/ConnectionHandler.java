package server;

import lib.SocketUtil;
import lib.message.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.kv.KeyValueStore;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static lib.SocketUtil.tryClose;
import static lib.message.MessageUtil.isValidKey;
import static lib.message.MessageUtil.isValidValue;

public class ConnectionHandler implements Runnable {
    final Socket s;
    private KeyValueStore db;
    final Logger logger = LogManager.getLogger(ConnectionHandler.class);

    public ConnectionHandler(Socket s, KeyValueStore db) {
        this.s = s;
        this.db = db;
    }

    @Override
    public void run() {
        OutputStream o = null;
        InputStream i = null;

        try {
            i = s.getInputStream();
            o = s.getOutputStream();

            String connectMessage = KVMessageMarshaller.marshall(MessageFactory.creatConnectionSuccessful());
            SocketUtil.sendMessage(o, connectMessage);

            while (SocketUtil.isConnected(s)) {
                String msg = SocketUtil.readMessage(i);
                KVMessage kvMessage = null;
                try {
                    kvMessage = KVMessageUnmarshaller.unmarshall(msg);
                    logger.debug(String.format(
                            "Got a message: %s <%s,%s>",
                            kvMessage.getStatus(), kvMessage.getKey(), kvMessage.getValue()
                    ));

                    validateKeyValueLength(kvMessage);

                    KVMessage response;
                    switch (kvMessage.getStatus()) {
                        case GET:
                            try {
                                String value = db.get(kvMessage.getKey());
                                response = MessageFactory.createGetSuccessMessage(kvMessage.getKey(), value);
                            } catch (KeyNotFoundException e) {
                                logger.info(String.format("Key '%s' not found", kvMessage.getKey()));
                                response = MessageFactory.createGetNotFoundMessage();
                            } catch (DbError e) {
                                logger.warn("Some error occured at database level", e);
                                response = MessageFactory.createGetErrorMessage();
                            }
                            break;
                        case PUT:
                            logger.debug(String.format("New PUT message from client: <%s,%s>", kvMessage.getKey(), kvMessage.getValue()));
                            try {
                                boolean updated = db.put(kvMessage.getKey(), kvMessage.getValue());
                                if (updated) {
                                    if (kvMessage.getValue() == null || kvMessage.getValue().equals(""))
                                        response = MessageFactory.createDeleteSuccessMessage();
                                    else
                                        response = MessageFactory.createPutUpdateMessage();
                                }
                                else
                                    response = MessageFactory.createPutSuccessMessage();
                            } catch (DbError e) {
                                logger.warn("PUT: Databaseerror!", e);
                                response = MessageFactory.createPutErrorMessage();
                            }
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
                } catch (InvalidKeyValueLengthException e) {
                    logger.info(String.format("Key or Value are too long. Only a size for key/value of 20/120kb is allowed. key=%S | value=%s", kvMessage.getKey(), kvMessage.getValue()));
                    new KVMessageImpl(null, null, KVMessage.StatusType.INVALID_MESSAGE);
                }
            }
        } catch (Exception e) {
            logger.warn("Error during communication with an open connection:" + e.getMessage(), e);
        } finally {
            tryClose(o);
            tryClose(i);
            tryClose(s);
        }
    }

    private void validateKeyValueLength(KVMessage message) throws InvalidKeyValueLengthException {
        switch (message.getStatus()) {
            case GET:
            case PUT:
            case DELETE: {
                if (!isValidKey(message.getKey()) || !isValidValue(message.getValue())) {
                    throw new InvalidKeyValueLengthException("Key or Value are too long. Only a size for key/value of 20/120kb is allowed");
                }
            }
            default:
                return;

        }
    }
}

