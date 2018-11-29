package ecs.command;

import ecs.Command;
import ecs.State;
import ecs.service.KvService;
import ecs.service.SshService;
import lib.hash.HashUtil;
import lib.message.Messaging;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import lib.server.CacheType;
import lib.server.RunningState;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

/**
 * This class represents the command for adding a new server
 */
public class AddServerCommand implements Command {
    private String name;
    private final String host;
    private final int port;
    private final CacheType cacheType;
    private final int cacheSize;

    public AddServerCommand(String name, String host, int port, CacheType cacheType, int cacheSize) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.cacheType = cacheType;
        this.cacheSize = cacheSize;
    }

    /**
     *Executes the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {
        BigInteger hash = null;
        try {
            hash = HashUtil.getHash(name);
        } catch (NoSuchAlgorithmException e) {
        }
        ServerData newServer = new ServerData(name, host, port, hash);
        newServer.setCacheType(cacheType);
        newServer.setCacheSize(cacheSize);

        Messaging newServerCon = new Messaging();
        RunningState newServerState;
        try {
            newServerCon.connect(newServer.getHost(), newServer.getPort());
            newServerState = KvService.getStatus(newServer);
        } catch (Exception e) {
            System.out.println("Could not connect to server: " + newServer.toString());
            // ## start server
            System.out.println("Starting up the server process (waiting for 15s)...");
            try {
                SshService.startKvServer(newServer);
                Thread.sleep(15000);
                newServerState = KvService.getStatus(newServer);
            } catch (Exception ex) {
                System.out.println("Exception while starting the server: " + ex.getMessage());
                return;
            }
        }
        if (newServerState != RunningState.UNCONFIGURED) {
            try {
                KvService.makeReadonly(newServer, newServerCon);
            } catch (Exception e) {
                System.out.println("Error. While making the server readonly");
                return;
            }
        }

        if (state.storeMeta.getKvServerList().size() == 0) {
            state.storeMeta.getKvServerList().add(newServer);
            System.out.println("Was added as first server to the list. Is unconfigure");
            return;
        }

        ServerData influencedServer = null;
        RunningState influencedStatus;
        try {
            influencedServer = state.storeMeta.findNextKvServer(hash);
        } catch (KVServerNotFoundException e) {}
        try {
            influencedStatus = KvService.getStatus(influencedServer);
        } catch (Exception e) {
            System.out.println("Could not reach server influenced server: " + influencedServer.toString());
            return;
        }
        if (influencedStatus == RunningState.UNCONFIGURED) {
            System.out.println("Servers seem to be unconfigured. You still have work to do");
            return;
        }

        Messaging influencedServerCon = new Messaging();
        try {
            influencedServerCon.connect(influencedServer.getHost(), influencedServer.getPort());
        } catch (IOException e) {
            System.out.println("Could not connect to server: " + influencedServer.toString());
            return;
        }
        try {
            KvService.makeReadonly(influencedServer, influencedServerCon);

            // #### move data
            state.storeMeta.getKvServerList().add(newServer);
            new ConfigureAllCommand().execute(state);

            KvService.moveData(newServer, influencedServerCon, true);

            new StartServersCommand().execute(state);
        } catch (Exception e) {
            System.out.println("Error while moving data" + e.getMessage());
            return;
        }

        System.out.println("Move relevant data to the new node!");
    }
}
