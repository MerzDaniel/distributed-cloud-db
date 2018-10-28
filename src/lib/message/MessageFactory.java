package lib.message;

public class MessageFactory {
    public static KVMessage createGetMessage(String key) {
        return new KVMessageImpl(key, null, KVMessage.StatusType.GET);
    }
    public static KVMessage createPutMessage(String key, String value) {
        return new KVMessageImpl(key, value, KVMessage.StatusType.PUT);
    }
}
