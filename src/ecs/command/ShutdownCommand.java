package ecs.command;

import ecs.Command;
import ecs.State;
import lib.message.admin.KVAdminMessage;
import lib.message.exception.MarshallingException;
import lib.message.Messaging;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * This class represent the command for shutting down a {@link server.KVServer} instance
 */
public class ShutdownCommand implements Command {
    Logger logger = LogManager.getLogger(ShutdownCommand.class);

    /**
     * Execute the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {

        state.storeMeta.getKvServerList().stream().parallel().forEach(s -> {
            KVAdminMessage stopMsg = new KVAdminMessage(KVAdminMessage.StatusType.SHUT_DOWN);
            Messaging con = new Messaging();
            boolean success = false;
            try {
                con.connect(s.getHost(), s.getPort());
                con.sendMessage(stopMsg);
                KVAdminMessage msg = (KVAdminMessage) con.readMessage();
                success = msg.status == KVAdminMessage.StatusType.SHUT_DOWN_SUCCESS;
            } catch (IOException e) {
                logger.warn("Error", e);
            } catch (MarshallingException e) {
                logger.warn("Error", e);
            }
            if (!success)
                System.out.format("Server %s %s:%d could not successfuly be shutdown\n", s.getName(), s.getHost(), s.getPort());
            else
                System.out.format("Server %s %s:%d was shutdown\n", s.getName(), s.getHost(), s.getPort());
        });
    }
}
