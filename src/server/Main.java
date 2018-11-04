package server;

import org.apache.commons.cli.*;
import server.kv.CacheType;

/**
 * The main class of the Server which has the main method to start a server for a given port
 */
public class Main {
    public static void main(String[] args) {
        CommandLineParser c = new DefaultParser();
        Options options = new Options();
        options.addOption(new Option("h", "help", false, "show help text"));
        options.addOption(new Option("p", "port", true, "port of the server"));
        options.addOption(new Option(
                "c", "cache-type", true,
                "Cachetype to use. possible values: FIFO,LRU,LFU,NONE(default)"
        ));
        options.addOption(new Option(
                "s", "cache-size", true,
                "Cache size to use (number of key,value pairs that will be hold in cache)"
        ));

        CommandLine cmd = null;
        try {
            cmd = c.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Wrong argument.");
            printHelp(options);
            System.exit(1);
        }

        if (cmd.hasOption("h")) {
            printHelp(options);
            System.exit(0);
        }

        int port = Integer.parseInt(cmd.getOptionValue("p", "50000"));
        int cacheSize = Integer.parseInt(cmd.getOptionValue("s", "10"));
        CacheType type = CacheType.NONE;
        try {
            type = CacheType.valueOf(cmd.getOptionValue("c", "NONE"));
        } catch(IllegalArgumentException e) {
            System.out.println("Wrong argument.");
            printHelp(options);
            System.exit(1);
        }

        new Server(port, cacheSize, type).run();
    }

    private static void printHelp(Options options) {
        new HelpFormatter().printHelp("java -jar server.jar", options);
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2-server.properties.xml");
    }
}
