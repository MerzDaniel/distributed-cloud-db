package client.store;

import lib.message.IMessage;
import lib.message.graph.GraphDbMessage;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.graph.query.QueryMessageImpl;
import lib.message.graph.response.ResponseMessageImpl;
import lib.message.kv.KVMessage;
import lib.message.exception.MarshallingException;
import lib.message.kv.KvMessageFactory;
import lib.message.Messaging;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * A client for the KeyValueStoreServer. It can connect to the server and do GET/PUT/DELETE requests
 */
public class KVStore implements KVCommInterface {
    public KVStoreMetaData kvStoreMetaData;
    final Messaging messaging;

    private final Logger logger = LogManager.getLogger(KVStore.class);

    /**
     * Creates a new KVStore
     *
     * @param kvStoreMetaData storeMeta data about the KVStore
     */
    public KVStore(KVStoreMetaData kvStoreMetaData) {
        this.kvStoreMetaData = kvStoreMetaData;
        this.messaging = new Messaging();
    }

    /**
     * Connects to the backend server
     *
     * @param host the host to be connected to
     * @param port the port of the host
     * @return true if successfully connected otherwise false
     * @throws IOException if any error occurred while establishing the connection
     */
    public boolean connect(String host, int port) throws IOException {
        return this.messaging.connect(host, port);
    }

    /**
     * Returns whether the client is connected to the backend
     *
     * @return the connection status
     */
    public boolean isConnected() {
        return messaging.isConnected();
    }

    /**
     * Disconnect the client from the backend
     */
    public void disconnect() {
        this.messaging.disconnect();
    }

    /**
     * Get the KVMessage for the {@code key} from backend
     *
     * @param key
     * @return KVMessage with information about operation success or failure
     * @throws IOException          if any I/O error happens
     * @throws MarshallingException if any error happens during the unmarshall process
     */
    public KVMessage get(String key) throws IOException, MarshallingException, KVServerNotFoundException  {
        KVMessage kvMessageRequest = KvMessageFactory.createGetMessage(key);

        ServerData responsibleServer = kvStoreMetaData.findRandomResponsibleForGet(key);
        boolean connectSuccess = this.connect(responsibleServer.getHost(), responsibleServer.getPort());

        if (!connectSuccess) return KvMessageFactory.createConnectErrorMessage();

        this.messaging.sendMessage(kvMessageRequest);
        KVMessage response = (KVMessage) this.messaging.readMessage();

        if (response.getStatus() == KVMessage.StatusType.SERVER_NOT_RESPONSIBLE) {
            applyNewMetadata(response);
            return get(key);
        }

        return response;
    }

    /**
     * Put the given {@code key} and {@code value} in the backend database
     *
     * @param key
     * @param value
     * @return KVMessage with information about operation success or failure
     * @throws IOException          if any I/O error happens
     * @throws MarshallingException if any error happens during the unmarshall process
     */
    public KVMessage put(String key, String value) throws IOException, MarshallingException, KVServerNotFoundException, NoSuchAlgorithmException {
        KVMessage kvMessageRequest = KvMessageFactory.createPutMessage(key, value);

        ServerData serverServerData = kvStoreMetaData.findKVServerForKey(key);
        boolean connectSuccess = this.connect(serverServerData.getHost(), serverServerData.getPort());

        if (!connectSuccess) return KvMessageFactory.createConnectErrorMessage();

        this.messaging.sendMessage(kvMessageRequest);
        KVMessage response = (KVMessage) this.messaging.readMessage();

        if (response.getStatus() == KVMessage.StatusType.SERVER_NOT_RESPONSIBLE) {
            applyNewMetadata(response);
            return put(key, value);
        }

        return response;
    }

    /**
     * Get the ResponseMessageImpl for the {@code key} from backend
     *
     * @return ResponseMessageImpl with information about operation success or failure
     * @throws IOException          if any I/O error happens
     * @throws MarshallingException if any error happens during the unmarshall process of response query
     */
    public ResponseMessageImpl query(QueryMessageImpl queryMessage) throws IOException, MarshallingException {
        return sendGraphMessage(queryMessage);
    }

    /**
     * Get the ResponseMessageImpl for the {@code mutationMessage} from backend
     *
     * @return ResponseMessageImpl with information about operation success or failure
     * @throws IOException          if any I/O error happens
     * @throws MarshallingException if any error happens during the unmarshall process of response query
     */
    public ResponseMessageImpl mutate(MutationMessageImpl mutationMessage) throws IOException, MarshallingException {
        return sendGraphMessage(mutationMessage);
    }

    private void applyNewMetadata(KVMessage response) {
        logger.debug(String.format("This server is not responsible for the key %s", response.toString()));
        try {
            kvStoreMetaData = KVStoreMetaData.unmarshall(response.getValue());
            logger.debug("The kvstore storeMeta data is updated");
        } catch (MarshallingException e) {
            logger.error("Error occurred during unmarshalling storeMeta data", e);
        }
    }

    private ResponseMessageImpl sendGraphMessage(GraphDbMessage message) throws IOException, MarshallingException{

        ServerData kvServer = kvStoreMetaData.getKvServerList().get(0);
        boolean connectSuccess = this.connect(kvServer.getHost(), kvServer.getPort());

        if (!connectSuccess) return new ResponseMessageImpl("Connection error!");

        this.messaging.sendMessage(message);
        IMessage response = this.messaging.readMessage();

        //errors SERVER_STOPPED and SERVER_WRITE_LOCK
        if (response instanceof KVMessage) {
            return new ResponseMessageImpl(((KVMessage) response).getStatus().name());
        }

        return (ResponseMessageImpl) response;
    }
}
