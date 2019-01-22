package lib.message.KvMessage;

import lib.message.exception.MarshallingException;
import lib.message.MessageMarshaller;

/**
 * The representation of the data passed between the client and the server
 */
public class KVMessageImpl implements KVMessage {
    private final String key;
    private final String value;
    private final KVMessage.StatusType statusType;

    public KVMessageImpl(String key, String value, KVMessage.StatusType statusType) {
        this.key = key;
        this.value = value;
        this.statusType = statusType;
    }
    public KVMessageImpl(KVMessage.StatusType statusType) {
        this.key = null;
        this.value = null;
        this.statusType = statusType;
    }

    /**
     * @return the key that is associated with this message,
     * null if not key is associated.
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * @return the value that is associated with this message,
     * null if not value is associated.
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * @return a status string that is used to identify request types,
     * response types and error types associated to the message.
     */
    @Override
    public StatusType getStatus() {
        return statusType;
    }

    /**
     * Returns whether the message is an error
     * @return true if this message has an error status
     */
    @Override
    public boolean isError() {
        switch (this.statusType) {
            case GET_ERROR:
                return true;
            case PUT_ERROR:
                return true;
            case DELETE_ERROR:
                return true;
            case GET_NOT_FOUND:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns whether this message is a success
     * @return true if this message has an success status
     */
    @Override
    public boolean isSuccess() {
        switch (this.statusType) {
            case GET_SUCCESS:
                return true;
            case PUT_SUCCESS:
                return true;
            case DELETE_SUCCESS:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s<%s,%s>", getStatus(), getKey(), getValue());
    }

    @Override
    public String marshall() throws MarshallingException {
        return MessageMarshaller.marshall(this);
    }
}
