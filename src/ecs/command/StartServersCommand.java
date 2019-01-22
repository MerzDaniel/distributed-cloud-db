package ecs.command;

import ecs.State;
import lib.message.admin.KVAdminMessage;
import lib.message.Messaging;
import lib.metadata.ServerData;

/**
 * This class represent the command for starting a {@link server.KVServer} instance
 */
public class StartServersCommand implements ecs.Command {

    /**
     * Execute the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {
        boolean universeIsOk = true;

        for (ServerData sd : state.storeMeta.getKvServerList()) {
            try {
                Messaging s = new Messaging();
                s.connect(sd.getHost(), sd.getPort());
                KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.START);
                s.sendMessage(msg);

                KVAdminMessage response = (KVAdminMessage) s.readMessage();
                if (response.status != KVAdminMessage.StatusType.START_SUCCESS) {
                    System.out.println(String.format("Error while starting server %s:%d : %s", sd.getHost(), sd.getPort(), response.status));
                    universeIsOk = false;
                }

            } catch (Exception e) {
                System.out.println(String.format("Error while starting server %s:%d : %s", sd.getHost(), sd.getPort(), e.getMessage()));
            }
        }

        if (universeIsOk)
            System.out.println("Successfully started all servers");
        else
            System.out.println("All other servers were started");
    }
}
