package ecs.command;

import ecs.Command;
import ecs.State;
import lib.TimeWatch;
import lib.message.KVAdminMessage;
import lib.message.Messaging;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import static ecs.service.KvService.moveData;

/**
 * This class represent the command for removing a {@link server.KVServer} instance
 */
public class RemoveNodeCommand implements Command {
    private Logger logger = LogManager.getLogger(RemoveNodeCommand.class);

    /**
     * Constructor to create a {@link RemoveNodeCommand} instance
     *
     */
    public RemoveNodeCommand() {
    }

    /**
     * Execute the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {
        if (state.storeMeta.getKvServerList().size() <= 0) {
            System.out.println("Cannot remove a node when less than 2 are running");
            return;
        }

        Collections.shuffle(state.storeMeta.getKvServerList());
        ServerData from = state.storeMeta.getKvServerList().remove(0);

        ServerData to = null;
        try {
            to = state.storeMeta.findNextKvServerByHash(from.getFromHash());
        } catch (KVServerNotFoundException e) {
            logger.error("Error.", e);
            System.out.println("Error while removing the server.");
        }

        if (from == null || to == null) {
            System.out.println("Server does not exist and can therefor not be removed.");
            return;
        }

        Messaging con = new Messaging();
        try {
            con.connect(from.getHost(), from.getPort());
        } catch (IOException e) {
            logger.warn("Error.", e);
            System.out.println("Error while connecting to the server.");
            return;
        }

        try {
            // move data
            TimeWatch moveTimer = TimeWatch.start();
            KVAdminMessage response = moveData(to, con, false);
            String responseString;
            if (response.status != KVAdminMessage.StatusType.MOVE_SUCCESS)
                System.out.println("Data was not successfully moved. But I'm evil and still shutting down the node ^_^");
            System.out.format("Move data took %dms.\n", moveTimer.time());

            // reconfigure
            state.storeMeta.getKvServerList().remove(from);
            new ConfigureAllCommand().execute(state);

            // stop
            KVAdminMessage stopMsg = new KVAdminMessage(KVAdminMessage.StatusType.SHUT_DOWN);
            con.sendMessage(stopMsg);
            response = (KVAdminMessage) con.readMessage();
            if (response.status != KVAdminMessage.StatusType.SHUT_DOWN_SUCCESS)
                System.out.println("Was not successfully shutdown");
            else
                System.out.println("Successfully shutdown the node");

        } catch (Exception e) {
            logger.warn("Error.", e);
            System.out.println("Error during removing the server and moving the data");
            return;
        }
    }

}
