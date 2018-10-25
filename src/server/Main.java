package server;

public class Main {
    public static void main(String[] args) {
        int port;
        if (args.length >0) {
            port = Integer.parseInt(args[0]);
        }
        else port = 50000;

        new Server(port).run();
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2-server.properties.xml");
    }
}
