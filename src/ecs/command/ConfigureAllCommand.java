package ecs.command;

import ecs.Command;
import ecs.State;
import ecs.service.KvService;
import lib.metadata.KVStoreMetaData;

/**
 * This class represents  the command for configure all {@link server.KVServer} instances
 */
public class ConfigureAllCommand implements Command {

    /**
     * Executes the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {
        boolean universeIsOk = true;

        KVStoreMetaData meta = state.storeMeta;

        universeIsOk = KvService.configureAll(meta);

        if (universeIsOk)
            System.out.println("Successfully configured all servers");
        else
            System.out.println("All other servers were configured successfully");
    }
}
