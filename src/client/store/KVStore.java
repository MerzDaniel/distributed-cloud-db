package client.store;

import client.communication.Connection;
import lib.message.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * A client for the KeyValueStoreServer. It can connect to the server and do GET/PUT/DELETE requests
 */
public class KVStore implements KVCommInterface{
    final String host;
    final int port;
    final Connection connection;

    private final Logger logger = LogManager.getLogger(KVStore.class);

    /**
     * Creates a new KVStore
     *
     * @param host the host name
     * @param port the port
     */
    public KVStore(String host, int port) {
        this.host = host;
        this.port = port;
        this.connection = new Connection();
    }

    /**
     * Connects to the backend server
     *
     * @return true if successfully connected otherwise false
     * @throws IOException if any error occurred while establishing the connection
     */
    public boolean connect() throws IOException {
        this.connection.connect(host, port);

        boolean success = true;
        String message = connection.readMessage();
        try {
            KVMessage kvM = KVMessageMarshaller.unmarshall(message);
            success = kvM.getStatus() == KVMessage.StatusType.CONNECT_SUCCESSFUL;
        } catch (UnmarshallException e) {
            logger.warn(String.format("KVServer %s:%d returned an invalid response: '%s'", host, port, message));
            disconnect();
            success = false;
        }
        if (!success) disconnect();
        return success;
    }

    /**
     * Returns whether the client is connected to the backend
     *
     * @return the connection status
     */
    public boolean isConnected() {
        return connection.isConnected();
    }

    /**
     * Disconnect the client from the backend
     */
    public void disconnect() {
        this.connection.disconnect();
    }

    /**
     * Get the KVMessage for the {@code key} from backend
     *
     * @param key
     * @return KVMessage with information about operation success or failure
     * @throws IOException if any I/O error happens
     * @throws UnmarshallException if any error happens during the unmarshall process
     */
    public KVMessage get(String key) throws IOException, UnmarshallException {
        KVMessage kvMessageRequest = MessageFactory.createGetMessage(key);
        this.connection.sendMessage(KVMessageMarshaller.marshall(kvMessageRequest));
        String response = this.connection.readMessage();
        return KVMessageMarshaller.unmarshall(response);
    }

    /**
     * Put the given {@code key} and {@code value} in the backend database
     *
     * @param key
     * @param value
     * @return KVMessage with information about operation success or failure
     * @throws IOException if any I/O error happens
     * @throws UnmarshallException if any error happens during the unmarshall process
     */
    public KVMessage put(String key, String value) throws IOException, UnmarshallException {
        KVMessage kvMessageRequest = MessageFactory.createPutMessage(key, value);
        this.connection.sendMessage(KVMessageMarshaller.marshall(kvMessageRequest));
        String response = this.connection.readMessage();

        return KVMessageMarshaller.unmarshall(response);
    }
}
