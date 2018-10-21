package ui;

import communication.Connection;

public class ConnectCommand implements Command{
    private String url;
    private int port;

    public ConnectCommand(String url, int port){
        this.port = port;
        this.url = url;
    }

    @Override
    public void execute(ApplicationState state) {
        state.connection = new Connection();
        state.connection.connect(this.url, this.port);
    }
}
