package lib.message;

public class MessageFactory {
    public static KVMessage createGetMessage(String key) {
        return new KVMessageImpl(key, null, KVMessage.StatusType.GET);
    }
    public static KVMessage createGetSuccessMessage() {
        return new KVMessageImpl(KVMessage.StatusType.GET_SUCCESS);
    }
    public static KVMessage createGetErrorMessage() {
        return new KVMessageImpl(KVMessage.StatusType.GET_ERROR);
    }
    public static KVMessage createPutMessage(String key, String value) {
        return new KVMessageImpl(key, value, KVMessage.StatusType.PUT);
    }
    public static KVMessage createPutSuccessMessage() {
        return new KVMessageImpl(KVMessage.StatusType.PUT_SUCCESS);
    }
    public static KVMessage createPutErrorMessage() {
        return new KVMessageImpl(KVMessage.StatusType.PUT_ERROR);
    }
    public static KVMessage createInvalidMessage() {
        return new KVMessageImpl(KVMessage.StatusType.INVALID_MESSAGE);
    }
}
