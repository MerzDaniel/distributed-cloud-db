package client.store;

import client.communication.Connection;
import lib.message.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

import static client.ui.Util.writeLine;

public class KvStore {
    final String host;
    final int port;
    final Connection connection;

    private final Logger logger = LogManager.getLogger(KvStore.class);

    public KvStore(String host, int port) {
        this.host = host;
        this.port = port;
        this.connection = new Connection();
    }

    public void connect() throws IOException {
        this.connection.connect(host, port);
    }

    public void disconnect() {
        if (!connection.isConnected()) {
            writeLine("Currently not connected to a server");
            return;
        }
        this.connection.disconnect();
    }

    public KVMessage get(String key) throws IOException {
        if (!connection.isConnected()) {
            writeLine("Currently not connected to a server");
            return MessageFactory.createGetErrorMessage();
        }

        KVMessage kvMessageRequest = MessageFactory.createGetMessage(key);
        this.connection.sendMessage(KVMessageMarshaller.marshall(kvMessageRequest));
        String response = this.connection.readMessage();
        try {
            return KVMessageUnmarshaller.unmarshall(response);
        } catch (UnmarshallException e) {
            logger.warn("Invalid response from the server.");
            writeLine("Response from the server was invalid.");
            return MessageFactory.createInvalidMessage();
        }
    }

    public KVMessage put(String key, String value) throws IOException {
        KVMessage kvMessageRequest = MessageFactory.createPutMessage(key, value);
        this.connection.sendMessage(KVMessageMarshaller.marshall(kvMessageRequest));
        String response = this.connection.readMessage();
        try {
            return KVMessageUnmarshaller.unmarshall(response);
        } catch (UnmarshallException e) {
            logger.warn("Got an invalid message.");
            writeLine("The response of the server was invalid");
            return MessageFactory.createInvalidMessage();
        }
    }
}
