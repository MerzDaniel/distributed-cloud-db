package client.ui.commands;

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
        boolean connected = state.connection.connect(this.url, this.port);

        if (connected){
            writeLine(state.connection.readMessage());
        }
        else{
            writeLine("Could not connect to the server " + url + ":" + port);
        }
    }
}