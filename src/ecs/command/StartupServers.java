package ecs.command;

import ecs.Command;
import ecs.State;
import lib.message.MarshallingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

import static ecs.service.SshService.startKvServer;
import static ecs.service.StatusService.getStatus;

public class StartupServers implements Command {
    private Logger l = LogManager.getLogger(StopServersCommand.class);
    @Override
    public void execute(State state) {
        state.meta.getKvServerList().stream().parallel().forEach(sd -> {
            try {
                getStatus(sd);
                return; // server can be reached therefor doesn't need to be started
            } catch (IOException e) {
                System.out.format("Server %s at %s:%d has could not be reached\n", sd.getName(), sd.getHost(), sd.getPort());
            } catch (MarshallingException e) {
                l.warn("Error", e);
                System.out.format("Server %s at %s:%d returned an error\n", sd.getName(), sd.getHost(), sd.getPort());
            }

            try {
                startKvServer(sd);
            } catch (Exception e) {
                System.out.format("Could not start server %s : %s\n", sd.toString(), e.getMessage());
            }
        });
    }
}
