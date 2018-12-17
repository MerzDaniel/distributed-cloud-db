package ecs.command;

import ecs.Command;
import ecs.State;
import ecs.service.KvService;
import lib.message.MarshallingException;
import lib.metadata.ServerData;
import lib.server.RunningState;
import lib.server.TimedRunningState;

import java.util.LinkedList;
import java.util.List;

public class FailOverCommand implements Command {
    @Override
    public void execute(State state) {
        List<ServerData> failedServers = new LinkedList<>();
        try {
            KvService.gossipServers(state.storeMeta.getKvServerList(), state.timedRunningStateMap);
            failedServers = new LinkedList<>();
            for (ServerData sd : state.storeMeta.getKvServerList()) {
                TimedRunningState timedRunningState = state.timedRunningStateMap.get(sd.getName());
                if (timedRunningState.runningState == RunningState.DOWN)
                    failedServers.add(sd);
            }

            if (failedServers.size() == 0) {
                System.out.println("The system seems to be running fine!");
                return;
            }

            if (failedServers.size() > 1) {
                System.out.println("Two many servers failed!!!" );
                System.out.println("Data cannot be recovered, please set everything up again, manually.");
                return;
            }
        } catch (MarshallingException e) {
            e.printStackTrace();
        }

        System.out.println("Oh no, one server failed! Will try to recover the data and ensure full replication!");
        try {
            KvService.removeNode(failedServers.get(0),state.storeMeta);
        } catch (Exception e) {
            System.out.println("Some error occured while recovering stuff.... ");
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Successfully ensured full replication again :)");
    }
}
