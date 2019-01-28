package ecs.command;

import ecs.Command;
import ecs.State;
import lib.message.exception.MarshallingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

import static ecs.service.SshService.startKvServer;
import static ecs.service.KvService.getStatus;

public class StartupServers implements Command {
    private Logger l = LogManager.getLogger(StopServersCommand.class);
    @Override
    public void execute(State state) {
        state.storeMeta.getKvServerList().stream().parallel().forEach(sd -> {
            try {
                getStatus(sd);
                System.out.format("%s is already startup.\n", sd.toString());
                return; // server can be reached therefor doesn't need to be started
            } catch (IOException e) {
                // we have to start the server
            } catch (MarshallingException e) {
                l.warn("Error", e);
                System.out.format("Server %s returned an error\n", sd.toString());
                return;
            }

            try {
                System.out.format("Starting up %s ...\n", sd.toString());
                startKvServer(sd, state.sshUsername);
            } catch (Exception e) {
                System.out.format("Could not start server %s : %s\n", sd.toString(), e.getMessage());
            }
        });
    }
}
