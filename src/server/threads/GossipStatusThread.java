package server.threads;

import lib.metadata.ServerData;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.ServerState;

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

            } catch (InterruptedException e) {}
        }
    }
}
