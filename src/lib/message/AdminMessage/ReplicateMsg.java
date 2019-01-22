package lib.message.AdminMessage;

public class ReplicateMsg extends KVAdminMessage {
    public final String replicateKey;
    public final String replicateValue;
    public final String srcServerName;

    public ReplicateMsg(String replicateKey, String replicateValue, String srcServerName) {
        super(StatusType.PUT_REPLICATE);
        this.replicateKey = replicateKey;
        this.replicateValue = replicateValue;
        this.srcServerName = srcServerName;
    }
}
