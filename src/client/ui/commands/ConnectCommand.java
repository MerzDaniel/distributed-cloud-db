package client.ui.commands;

import client.store.KVStore;
import client.ui.ApplicationState;
import client.ui.Command;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

import static client.ui.Util.writeLine;

/**
 * Connects to a server.
 */
public class ConnectCommand implements Command {
    private String url;
    private int port;

    Logger logger = LogManager.getLogger(ConnectCommand.class);

    public ConnectCommand(String url, int port){
        this.port = port;
        this.url = url;
    }

    @Override
    public void execute(ApplicationState state) {
        ServerData serverServerData = new ServerData(this.url, this.port);
        state.kvStore = new KVStore(new KVStoreMetaData(Arrays.asList(serverServerData)));
        boolean success;
        try {
            success = state.kvStore.connect(this.url, this.port);
        } catch (IOException e) {
            logger.warn("Error while connecting", e);
            success = false;
        }
        if (!success) {
            writeLine("There was an error while connecting to the server :(");
            return;
        }

        writeLine("Successfully connected to the server :)");
    }
}
