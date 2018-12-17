package ecs.command;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import ecs.Command;
import ecs.State;

import ecs.service.KvService;
import lib.message.KVAdminMessage;
import lib.message.MarshallingException;
import lib.message.Messaging;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.CacheType;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import static ecs.service.SshService.startKvServer;
import static ecs.service.KvService.getStatus;
import static ecs.service.KvService.configure;


/**
 * This class represents  the command for initialize {@link server.KVServer} instances
 */
public class InitCommand implements Command {
    int noOfServers;
    int cacheSize;
    CacheType cacheType;

    private Logger logger = LogManager.getLogger(StopServersCommand.class);

    public InitCommand(int noOfServers, int cacheSize, CacheType cacheType) {
        this.noOfServers = noOfServers;
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
    }

    /**
     * Executes the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {
        boolean universeIsOk = true;

        Collections.shuffle(state.poolMeta.getKvServerList());

        if (noOfServers > state.poolMeta.getKvServerList().size()) {
            System.out.println(String.format("Requested no of servers (%d) exceeds the available servers (%d)", noOfServers, state.poolMeta.getKvServerList().size()));
            return;
        }

        state.storeMeta = new KVStoreMetaData();
        state.storeMeta.getKvServerList().addAll(state.poolMeta.getKvServerList().subList(0, noOfServers));

        for (int index = 0; index < state.storeMeta.getKvServerList().size(); index++) {
            ServerData sd = state.storeMeta.getKvServerList().get(index);
            sd.setCacheSize(this.cacheSize);
            sd.setCacheType(this.cacheType);

            try {
                RunningState serverState = getStatus(sd);

                if (serverState.equals(RunningState.RUNNING)){
                    System.out.format("%s is already running.\n", sd.toString());
                    continue;
                }

            } catch (IOException e) {
                //we need to start the server
            } catch (MarshallingException e) {
                universeIsOk = false;
                logger.warn("Error", e);
                System.out.format("Could not start server %s : %s\n", sd.toString(), e.getMessage());
                continue;
            }

            boolean serverCanBeReached = KvService.serverCanBeReached(sd);
            if (!serverCanBeReached) {
                try {
                    startKvServer(sd);
                } catch (JSchException | IOException | SftpException e) {
                    universeIsOk = false;
                    logger.warn("Error", e);
                    System.out.format("Could not start server %s : %s\n", sd.toString(), e.getMessage());
                    continue;
                }
            }

            KVAdminMessage response = null;
            try {
                Thread.sleep(3000);
                response = configure(sd, state.storeMeta, index);
                if (!response.status.equals(KVAdminMessage.StatusType.CONFIGURE_SUCCESS)) {
                    universeIsOk = false;
                    System.out.println(String.format("Error while init the server %s:%d : %s", sd.getHost(), sd.getPort(), response.status));
                }
            } catch (IOException | MarshallingException | InterruptedException e) {
                universeIsOk = false;
                logger.warn("Error", e);
                System.out.format("Could not start server %s : %s\n", sd.toString(), e.getMessage());
                continue;
            }
        }

        if (universeIsOk)
            System.out.println("Successfully initialized servers");
        else
            System.out.println("All other servers were configured successfully");

        //todo
        //store cluster   information to the cluster.config
        state.poolMeta.getKvServerList().removeAll(state.storeMeta.getKvServerList());
    }
}
