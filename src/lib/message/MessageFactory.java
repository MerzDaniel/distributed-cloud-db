package lib.message;

/**
 * This class provides the methods to create instances of {@link KVMessage}
 */
public class MessageFactory {
    public static KVMessage createGetMessage(String key) {
        return new KVMessageImpl(key, null, KVMessage.StatusType.GET);
    }
    public static KVMessage createGetSuccessMessage(String key, String value) {
        return new KVMessageImpl(key, value, KVMessage.StatusType.GET_SUCCESS);
    }
    public static KVMessage createGetErrorMessage() {
        return new KVMessageImpl(KVMessage.StatusType.GET_ERROR);
    }
    public static KVMessage createGetNotFoundMessage() {
        return new KVMessageImpl(KVMessage.StatusType.GET_NOT_FOUND);
    }
    public static KVMessage createPutMessage(String key, String value) {
        return new KVMessageImpl(key, value, KVMessage.StatusType.PUT);
    }
    public static KVMessage createPutSuccessMessage() {
        return new KVMessageImpl(KVMessage.StatusType.PUT_SUCCESS);
    }
    public static KVMessage createPutUpdateMessage() {
        return new KVMessageImpl(KVMessage.StatusType.PUT_UPDATE);
    }
    public static KVMessage createPutErrorMessage() {
        return new KVMessageImpl(KVMessage.StatusType.PUT_ERROR);
    }
    public static KVMessage createDeleteMessage(String key, String value) {
        return new KVMessageImpl(key, value, KVMessage.StatusType.DELETE);
    }
    public static KVMessage createDeleteSuccessMessage() {
        return new KVMessageImpl(KVMessage.StatusType.DELETE_SUCCESS);
    }
    public static KVMessage createDeleteErrorMessage() {
        return new KVMessageImpl(KVMessage.StatusType.DELETE_ERROR);
    }
    public static KVMessage createInvalidMessage() {
        return new KVMessageImpl(KVMessage.StatusType.INVALID_MESSAGE);
    }

    public static KVMessage creatConnectionSuccessful() {
        return new KVMessageImpl(KVMessage.StatusType.CONNECT_SUCCESSFUL);
    }

    public static KVMessage createConnectErrorMessage() {
        return new KVMessageImpl(KVMessage.StatusType.CONNECT_ERROR);
    }

    public static KVMessage creatServerStopped() { return new KVMessageImpl(KVMessage.StatusType.SERVER_STOPPED);}

    public static KVMessage createServerWriteLock() { return new KVMessageImpl(KVMessage.StatusType.SERVER_WRITE_LOCK);}
}
