package ecs.command;

import ecs.Command;
import ecs.State;
import lib.communication.Connection;
import lib.message.KVAdminMessage;
import lib.message.MarshallingException;
import lib.message.MessageMarshaller;
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
            Connection con = new Connection();
            boolean success = false;
            try {
                con.connect(s.getHost(), s.getPort());
                con.readMessage();
                con.sendMessage(stopMsg.marshall());
                KVAdminMessage msg = (KVAdminMessage) MessageMarshaller.unmarshall(con.readMessage());
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
