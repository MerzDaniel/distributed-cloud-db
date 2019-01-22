package lib.message;

import lib.message.admin.FullReplicationMsg;
import lib.message.admin.KVAdminMessage;
import lib.message.exception.MarshallingException;
import lib.server.RunningState;
import lib.server.TimedRunningState;
import lib.server.TimedRunningStateMap;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class MarshallingTest {
    @Test
    public void testGossipStatus() throws MarshallingException {

        TimedRunningStateMap timedRunningStateMap = new TimedRunningStateMap();
        timedRunningStateMap.put("server key", new TimedRunningState(1111, RunningState.RUNNING));
        timedRunningStateMap.put("server key 2", new TimedRunningState(2222, RunningState.UNCONFIGURED));

        KVAdminMessage gossipStatus = new KVAdminMessage(KVAdminMessage.StatusType.GOSSIP_STATUS, timedRunningStateMap);
        KVAdminMessage gossipStatusSuccess = new KVAdminMessage(KVAdminMessage.StatusType.GOSSIP_STATUS_SUCCESS, timedRunningStateMap);

        KVAdminMessage resultGossipStatus = (KVAdminMessage) MessageMarshaller.unmarshall(gossipStatus.marshall());
        KVAdminMessage resultGossipStatusSuccess = (KVAdminMessage) MessageMarshaller.unmarshall(gossipStatusSuccess.marshall());

        assertEquals(KVAdminMessage.StatusType.GOSSIP_STATUS, resultGossipStatus.status);
        assertEquals(timedRunningStateMap.getKeys().size(), resultGossipStatus.timedServerStates.getKeys().size());
        assertEquals(timedRunningStateMap.marshall(), resultGossipStatus.timedServerStates.marshall());

        assertEquals(KVAdminMessage.StatusType.GOSSIP_STATUS_SUCCESS, resultGossipStatusSuccess.status);
        assertEquals(timedRunningStateMap.getKeys().size(), resultGossipStatusSuccess.timedServerStates.getKeys().size());
        assertEquals(timedRunningStateMap.marshall(), resultGossipStatusSuccess.timedServerStates.marshall());
    }
    @Test
    public void testGossipStatusWithEmptyMap() throws MarshallingException {

        TimedRunningStateMap emptyMap = new TimedRunningStateMap();

        KVAdminMessage gossipStatus = new KVAdminMessage(KVAdminMessage.StatusType.GOSSIP_STATUS, emptyMap);

        KVAdminMessage resultGossipStatus = (KVAdminMessage) MessageMarshaller.unmarshall(gossipStatus.marshall());

        assertEquals(KVAdminMessage.StatusType.GOSSIP_STATUS, resultGossipStatus.status);
        assertEquals(emptyMap.getKeys().size(), resultGossipStatus.timedServerStates.getKeys().size());
        assertEquals(emptyMap.marshall(), resultGossipStatus.timedServerStates.marshall());
    }

    @Test
    public void testFullReplication() throws MarshallingException {
        FullReplicationMsg fullReplicationMsg = new FullReplicationMsg("srcDataServer", "targetServerName");
        FullReplicationMsg resultMsg = (FullReplicationMsg) MessageMarshaller.unmarshall(fullReplicationMsg.marshall());

        assertEquals(KVAdminMessage.StatusType.FULL_REPLICATE, resultMsg.status);
        assertEquals("srcDataServer", resultMsg.srcDataServerName);
        assertEquals("targetServerName", resultMsg.targetServerName);
    }
}
