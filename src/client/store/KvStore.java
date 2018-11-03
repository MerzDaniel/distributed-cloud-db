package client.store;

import client.communication.Connection;
import lib.message.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

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

    public boolean connect() throws IOException {
        this.connection.connect(host, port);

        boolean success = true;
        String message = connection.readMessage();
        try {
            KVMessage kvM = KVMessageUnmarshaller.unmarshall(message);
            success = kvM.getStatus() == KVMessage.StatusType.CONNECT_SUCCESSFUL;
        } catch (UnmarshallException e) {
            logger.warn(String.format("Server %s:%d returned an invalid response: '%s'", host, port, message));
            disconnect();
            success = false;
        }
        if (!success) disconnect();
        return success;
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public void disconnect() {
        this.connection.disconnect();
    }

    public KVMessage get(String key) throws IOException, UnmarshallException {
        KVMessage kvMessageRequest = MessageFactory.createGetMessage(key);
        this.connection.sendMessage(KVMessageMarshaller.marshall(kvMessageRequest));
        String response = this.connection.readMessage();
        return KVMessageUnmarshaller.unmarshall(response);
    }

    public KVMessage put(String key, String value) throws IOException, UnmarshallException {
        KVMessage kvMessageRequest = MessageFactory.createPutMessage(key, value);
        this.connection.sendMessage(KVMessageMarshaller.marshall(kvMessageRequest));
        String response = this.connection.readMessage();

        return KVMessageUnmarshaller.unmarshall(response);
    }
}
