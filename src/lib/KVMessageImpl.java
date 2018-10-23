package lib;

public class KVMessageImpl implements KVMessage {
    private String key;
    private String value;
    private KVMessage.StatusType statusType;

    public KVMessageImpl(String key, String value, KVMessage.StatusType statusType) {
        this.key = key;
        this.value = value;
        this.statusType = statusType;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public StatusType getStatus() {
        return statusType;
    }
}
