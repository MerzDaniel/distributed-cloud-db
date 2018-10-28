package client.ui.commands;

import client.store.KvStore;
import client.ui.ApplicationState;
import client.ui.Command;

import static client.ui.Util.writeLine;

public class ConnectCommand implements Command {
    private String url;
    private int port;

    public ConnectCommand(String url, int port){
        this.port = port;
        this.url = url;
    }

    @Override
    public void execute(ApplicationState state) {
        state.kvStore = new KvStore(this.url, this.port);
        boolean connected = state.kvStore.connect();
    }
}
