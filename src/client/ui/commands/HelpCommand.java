package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;

/**
 * Prints the help
 */
public class HelpCommand implements Command {
    @Override
    public void execute(ApplicationState state) {
        System.out.println("Usage:");
        System.out.println("connect <host> <port> : Connect to a host");
        System.out.println("put <key> <value>     : Put <key,value> to the data store. <key> should not have any whitespaces");
        System.out.println("get <key>             : Get <value> for the <key> from the data store");
        System.out.println("query <documentId> <property1>,<property2>, ...         " +
                                        ": Get the result <property1>,<property2>, ... for document of <documentId>");
        System.out.println("mutate <documentId> <property1>:<value1>,<property2>:<value2>, ...         " +
                ": Create a new document {property1:value1,property2:value2,...} with id <documentId>");
        System.out.println("logLevel <level>      : Set loglevel. Supported logLevels are ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF");
        System.out.println("disconnect            : Disconnects from the connected host");
        System.out.println("c                     : Fast connect to localhost:50000");
        System.out.println("help                  : Print this help text");
        System.out.println("quit                  : Exit application");
    }
}
