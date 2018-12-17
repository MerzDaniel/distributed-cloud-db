package ecs.command;

import ecs.Command;
import ecs.State;
import ecs.service.KvService;
import lib.message.MarshallingException;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

/**
 * This class represent the command for removing a {@link server.KVServer} instance
 */
public class RemoveNodeCommand implements Command {
    private String serverName;
    private Logger logger = LogManager.getLogger(RemoveNodeCommand.class);

    /**
     * Constructor to create a {@link RemoveNodeCommand} instance
     *
     */
    public RemoveNodeCommand() {
    }

    public RemoveNodeCommand(String serverName) {
        this.serverName = serverName;
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
        ServerData removedNode = state.storeMeta.getKvServerList().get(0);

        if (serverName != null) {
            try {
                removedNode = state.storeMeta.findKvServerByName(serverName);
            } catch (KVServerNotFoundException e) {
                logger.error("No server with the name", e);
            }
        }

        try {
            KvService.removeNode(removedNode, state);
        } catch (KVServerNotFoundException | IOException | MarshallingException e) {
            logger.error("There was an error while re-configure the system", e);
            System.out.println("There was an error while re-configure the system");
        }
    }


}
