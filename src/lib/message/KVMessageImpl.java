package lib.message;

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

    @Override
    public boolean isError() {
        switch (this.statusType) {
            case GET_ERROR:
                return true;
            case PUT_ERROR:
                return true;
            case DELETE_ERROR:
                return true;
            default:
                return false;
        }
    }

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
}
