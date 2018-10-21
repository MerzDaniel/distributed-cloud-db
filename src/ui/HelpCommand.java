package ui;

public class HelpCommand implements Command{
    @Override
    public void execute(ApplicationState state) {
        System.out.println("Usage:");
        System.out.println("connect <host> <port> : Connect to a host");
        System.out.println("send <msg>            : Send message to connected host");
        System.out.println("logLevel <level>      : Set loglevel");
        System.out.println("help                  : Print this help text");
        System.out.println("quit                  : Exit application");
    }
}
