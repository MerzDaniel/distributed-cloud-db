package lib.message;

import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;

/**
 * This is the absruct structure for the messages between KVServer and ECS
 */
public class KVAdminMessage implements IMessage {
    public final StatusType status;
    public KVStoreMetaData meta;
    public ServerData serverData;
    public RunningState runningState;
    public int currentServerIndex;

    public KVAdminMessage(StatusType status, RunningState rs) {
        this.status = status;
        runningState = rs;
    }

    public enum StatusType {
        CONFIGURE,
        CONFIGURE_SUCCESS,
        CONFIGURE_ERROR,
        START,                    /* Start a KVServer */
        START_SUCCESS,            /* Starting the KVServer is success */
        START_ERROR,            /* Starting the KVServer is not success  */
        STOP,                    /* Stop a KVServer */
        STOP_SUCCESS,           /* Stoping the KVServer is success */
        STOP_ERROR,            /* Stoping the KVServer is not success */
        SHUT_DOWN,                /* Shut down a KVServer */
        SHUT_DOWN_SUCCESS,      /* Shutting down the KVServer is success */
        SHUT_DOWN_ERROR,        /* Shutting down the KVServer is not success */
        /**
         * Move data
         */
        MOVE,
        MOVE_SUCCESS,
        MOVE_ERROR,
        STATUS,
        STATUS_RESPONSE,
    }

    /**
     * Create a ECSMesage
     *
     * @param status status of the message
     */
    public KVAdminMessage(StatusType status) {
        this.status = status;
    }

    public KVAdminMessage(StatusType status, ServerData content) {
        this.status = status;
        serverData = content;
    }

    public KVAdminMessage(StatusType status, KVStoreMetaData meta, int currentServerIndex) {
        this.status = status;
        this.meta = meta;
        this.currentServerIndex = currentServerIndex;
    }

    public String marshall() throws MarshallingException {
        return MessageMarshaller.marshall(this);
    }
}
