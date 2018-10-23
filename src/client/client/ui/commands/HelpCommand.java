package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;

import static client.ui.Util.writeLine;

public class HelpCommand implements Command {
    @Override
    public void execute(ApplicationState state) {
        writeLine("Usage:");
        writeLine("connect <host> <port> : Connect to a host");
        writeLine("send <msg>            : Send message to connected host");
        writeLine("logLevel <level>      : Set loglevel. Supported logLevels are ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF");
        writeLine("disconnect            : Disconnects from the connected host");
        writeLine("help                  : Print this help text");
        writeLine("quit                  : Exit application");
    }
}
