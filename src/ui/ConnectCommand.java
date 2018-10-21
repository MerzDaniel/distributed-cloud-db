package ui;

import static ui.Util.writeLine;

public class ConnectCommand implements Command{
    private String url;
    private int port;

    public ConnectCommand(String url, int port){
        this.port = port;
        this.url = url;
    }

    @Override
    public void execute(ApplicationState state) {
        String msg = state.connection.connect(this.url, this.port);
        writeLine(msg);
    }
}
