package client.ui.commands;

import client.store.KvStore;
import client.ui.ApplicationState;
import client.ui.Command;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

import static client.ui.Util.writeLine;

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
        state.kvStore = new KvStore(this.url, this.port);
        try {
            state.kvStore.connect();
        } catch (IOException e) {
            logger.warn("Error while connecting", e);
            writeLine("There was an error while connecting to the server :(");
            return;
        }

        writeLine("Successfully connected to the server :)");
    }
}
