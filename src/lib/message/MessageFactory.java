package lib.message;

public class MessageFactory {
    public static KVMessage createGetMessage(String key) {
        return new KVMessageImpl(key, null, KVMessage.StatusType.GET);
    }
    public static KVMessage createGetSuccessMessage(String key, String value) {
        return new KVMessageImpl(KVMessage.StatusType.GET_SUCCESS);
    }
    public static KVMessage createGetErrorMessage(String key, String value) {
        return new KVMessageImpl(KVMessage.StatusType.GET_ERROR);
    }
    public static KVMessage createPutMessage(String key, String value) {
        return new KVMessageImpl(key, value, KVMessage.StatusType.PUT);
    }
    public static KVMessage createPutSuccessMessage(String key, String value) {
        return new KVMessageImpl(KVMessage.StatusType.PUT_SUCCESS);
    }
    public static KVMessage createPutErrorMessage(String key, String value) {
        return new KVMessageImpl(KVMessage.StatusType.PUT_ERROR);
    }
    public static KVMessage createInvalidMessage(String key, String value) {
        return new KVMessageImpl(KVMessage.StatusType.INVALID_MESSAGE);
    }
}
