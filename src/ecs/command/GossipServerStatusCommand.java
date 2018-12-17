package ecs.command;

import ecs.Command;
import ecs.State;
import ecs.service.KvService;
import lib.message.MarshallingException;
import lib.metadata.ServerData;
import lib.server.TimedRunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * This class represent the command for getting the status of a {@link server.KVServer} instance
 */
public class GossipServerStatusCommand implements Command {
    Logger l = LogManager.getLogger(GossipServerStatusCommand.class);

    /**
     * Execute the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {
        Random random = new Random();
        List<ServerData> kvServerList = state.poolMeta.getKvServerList();

        try {
            state.timedRunningStateMap = KvService.gossipServers(state.poolMeta.getKvServerList(), state.timedRunningStateMap);
            kvServerList.forEach(sd2 -> {
                TimedRunningState timedRunningState = state.timedRunningStateMap.get(sd2.getName());
                String serverState;
                if (timedRunningState == null) serverState = "UNKNOWN";
                else serverState = String.format("%s (%s)", timedRunningState.runningState.name(), new Date(timedRunningState.accessTime).toString());
                System.out.format("State of Server %s: %s\n", sd2.getName(), serverState);
            });

        } catch (IOException e) {
            System.out.println("Gossip failed please try again");
            return;
        } catch (MarshallingException e) {
            System.out.format("Invalid formatted msg. Please try again.");
            return;
        }
    }
}
