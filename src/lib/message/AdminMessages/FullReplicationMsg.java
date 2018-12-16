package lib.message.AdminMessages;

import lib.message.KVAdminMessage;

public class FullReplicationMsg extends KVAdminMessage {

    public final String srcDataServerName;
    public final String targetServerName;

    public FullReplicationMsg(String srcDataServerName, String targetServerName) {
        super(StatusType.FULL_REPLICATE);

        this.srcDataServerName = srcDataServerName;
        this.targetServerName = targetServerName;
    }
}
