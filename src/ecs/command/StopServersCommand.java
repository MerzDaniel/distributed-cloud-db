package ecs.command;

import ecs.Command;
import ecs.State;
import lib.message.admin.KVAdminMessage;
import lib.message.Messaging;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;

public class StopServersCommand implements Command {
    Logger logger = LogManager.getLogger(StopServersCommand.class);

    @Override
    public void execute(State state) {
        logger.info("Stop all servers");

        state.storeMeta.getKvServerList().stream().parallel().forEach(sd -> {
            try {
                Messaging s = new Messaging();
                s.connect(sd.getHost(), sd.getPort());

                s.sendMessage(new KVAdminMessage(KVAdminMessage.StatusType.STOP));
                KVAdminMessage response = (KVAdminMessage) s.readMessage();
                if (response.status != KVAdminMessage.StatusType.STOP_SUCCESS) {
                    System.out.format("Error: Server %s %s:%d could NOT be put into IDLE mode\n", sd.getName(), sd.getHost(), sd.getPort());
                    return;
                }
                System.out.format("Server %s %s:%d is now in IDLE mode\n", sd.getName(), sd.getHost(), sd.getPort());
            } catch (UnknownHostException e) {
                logger.warn("Error", e);
                System.out.println("Unknown host: " + sd.getHost());
            } catch (IOException e) {
                logger.warn("Error", e);
                System.out.println("IO exception: " + e.getMessage());
            } catch (Exception e) {
                logger.warn("Error", e);
                System.out.println("Exception: " + e.getMessage());
            }
        });
        System.out.println("Done.");
    }
}
