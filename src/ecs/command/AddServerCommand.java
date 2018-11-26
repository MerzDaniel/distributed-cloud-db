package ecs.command;

import ecs.Command;
import ecs.State;
import ecs.service.KvService;
import lib.hash.HashUtil;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import lib.server.CacheType;
import lib.server.RunningState;

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

        if (state.meta.getKvServerList().size() == 0) {
            state.meta.getKvServerList().add(newServer);
            System.out.println("Was added as first server to the list.");
            return;
        }

        ServerData influencedServer = null;
        RunningState influencedStatus;
        try {
            influencedServer = state.meta.findNextKvServer(hash);
        } catch (KVServerNotFoundException e) {}
        try {
            influencedStatus = KvService.getStatus(influencedServer);
        } catch (Exception e) {
            System.out.println("Error while getting status of server! Did not move data or anything");
            return;
        }
        if (influencedStatus == RunningState.UNCONFIGURED) {
            System.out.println("Servers seem to be unconfigured. You still have work to do");
            return;
        }


    }
}
