package lib.message.AdminMessage;

import lib.message.IMessage;
import lib.message.exception.MarshallingException;
import lib.message.MessageMarshaller;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import lib.server.TimedRunningStateMap;

/**
 * This is the absruct structure for the messages between KVServer and ECS
 */
public class KVAdminMessage implements IMessage {
    public final StatusType status;
    public KVStoreMetaData meta;
    public ServerData serverData;
    public RunningState runningState;
    public int currentServerIndex;

    public String key;
    public String value;

    public TimedRunningStateMap timedServerStates;

    public KVAdminMessage(StatusType status, RunningState rs) {
        this.status = status;
        runningState = rs;
    }

    public KVAdminMessage(StatusType status, TimedRunningStateMap timedRunningStateMap) {
        this.status = status;
        this.timedServerStates = timedRunningStateMap;
    }

    public enum StatusType {
        INIT,
        CONFIGURE,
        CONFIGURE_SUCCESS,
        CONFIGURE_ERROR,
        START,                    /* Start a KVServer */
        START_SUCCESS,            /* Starting the KVServer is success */
        START_ERROR,            /* Starting the KVServer is not success  */
        MAKE_READONLY,
        MAKE_SUCCESS,
        MAKE_ERROR,
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
        MOVE_SOFT,
        MOVE_SUCCESS,
        MOVE_ERROR,
        DATA_MOVE,
        DATA_MOVE_SUCCESS,
        STATUS,
        STATUS_RESPONSE,
        PUT_REPLICATE,
        PUT_REPLICATE_SUCCESS,
        PUT_REPLICATE_ERROR,
        FULL_REPLICATE,
        FULL_REPLICATE_SUCCESS,
        FULL_REPLICATE_ERROR,
        DELETE_REPLICATE,
        DELETE_REPLICATE_SUCCESS,
        DELETE_REPLICATE_ERROR,
        GOSSIP_STATUS,
        GOSSIP_STATUS_SUCCESS,

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

    public KVAdminMessage(StatusType status, String key, String value) {
        this.status = status;
        this.key = key;
        this.value = value;
    }

    public KVAdminMessage(StatusType status, String key, String value, ServerData serverData) {
        this.status = status;
        this.key = key;
        this.value = value;
    }

    public KVAdminMessage(StatusType status, KVStoreMetaData meta, int currentServerIndex) {
        this.status = status;
        this.meta = meta;
        this.currentServerIndex = currentServerIndex;
    }

    @Override
    public String marshall() throws MarshallingException {
        return MessageMarshaller.marshall(this);
    }
}
