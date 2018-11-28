package ecs.command;

import ecs.Command;
import ecs.State;
import lib.message.KVAdminMessage;
import lib.message.MarshallingException;
import lib.metadata.ServerData;

import java.io.IOException;

import static ecs.service.KvService.configure;

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

        for (int index = 0; index < state.storeMeta.getKvServerList().size(); index++) {
            ServerData sd = state.storeMeta.getKvServerList().get(index);

            try {
                KVAdminMessage response = configure(sd, state.storeMeta, index);
                if (!response.status.equals(KVAdminMessage.StatusType.CONFIGURE_SUCCESS)) {
                    universeIsOk = false;
                    System.out.println(String.format("Error while configuring the server %s:%d : %s", sd.getHost(), sd.getPort(), response.status));
                }
            } catch (IOException | MarshallingException e) {
                universeIsOk = false;
                System.out.println(String.format("Error while configuring the server %s:%d : %s", sd.getHost(), sd.getPort(), e.getMessage()));
            }
        }

        if (universeIsOk)
            System.out.println("Successfully configured all servers");
        else
            System.out.println("All other servers were configured successfully");
    }
}
