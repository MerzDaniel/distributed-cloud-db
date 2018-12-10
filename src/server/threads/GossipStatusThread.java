package server.threads;

import lib.message.IMessage;
import lib.message.KVAdminMessage;
import lib.message.Messaging;
import lib.metadata.ServerData;
import lib.server.RunningState;
import lib.server.TimedRunningState;
import lib.server.TimedRunningStateMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;
import server.threads.util.gossip.RunningStates;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GossipStatusThread extends Thread {

    Logger logger = LogManager.getLogger(GossipStatusThread.class);
    final static long GOSSIP_INTERVAL = 1000 * 30;
    final static int GOSSIP_NODE_COUNT = 3;
    private ServerState state;

    @Override
    public void run() {
        Random random = new Random();
        while (state.runningState != RunningState.SHUTTINGDOWN) {
            try {
                Thread.sleep((long) (random.nextDouble() * GOSSIP_INTERVAL) + 1);
                if (state.runningState != RunningState.SHUTTINGDOWN) break;
                List<ServerData> servers = new LinkedList<>();
                int i=0, gossipServerCount;
                while(i < (gossipServerCount = Math.min(GOSSIP_NODE_COUNT, state.meta.getKvServerList().size()))) {
                    ServerData sd = state.meta.getKvServerList().get(random.nextInt(gossipServerCount));
                    if(servers.contains(sd)) continue; // try another random server
                    servers.add(sd);
                }

                state.stateOfAllServers.put(state.currentServerServerData.getName(), new TimedRunningState(state.runningState));
                IMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.GOSSIP_STATUS, state.stateOfAllServers);
                TimedRunningStateMap remoteServerStates = servers.stream().parallel().map(s -> {
                    Messaging messaging = new Messaging();
                    try {
                        messaging.connect(s);
                        messaging.sendMessage(msg);
                        KVAdminMessage response = (KVAdminMessage) messaging.readMessage();
                        if (response.status != KVAdminMessage.StatusType.GOSSIP_STATUS_SUCCESS)
                            throw new Exception("Wrong status: "+ response.status.name());

                        return response.timedServerStates;
                    } catch (Exception e) {
                        logger.warn("Problem while gossiping one of the servers", e);
                    }
                    return new TimedRunningStateMap();
                }).reduce(new TimedRunningStateMap(), RunningStates::combineMaps, RunningStates::combineMaps );

                RunningStates.combineMapsInto(state.stateOfAllServers, remoteServerStates, state.stateOfAllServers);

            } catch (InterruptedException e) {}
        }
    }
}