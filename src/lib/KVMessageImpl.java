package lib;

public class KVMessageImpl implements KVMessage {
    private String key;
    private String value;
    private KVMessage.StatusType statusType;

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
