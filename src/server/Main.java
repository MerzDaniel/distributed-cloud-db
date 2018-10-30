package server;

import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) throws ParseException {
        CommandLineParser c = new DefaultParser();
        Options options = new Options();
        options.addOption(new Option("help", "show help text"));
        options.addOption(new Option("p", "port", true, "port of the server"));

        CommandLine cmd = c.parse(options, args);

        int port = Integer.parseInt(cmd.getOptionValue("p", "50000"));;
        new Server(port).run();
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2-server.properties.xml");
    }
}
