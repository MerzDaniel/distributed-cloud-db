package server;

import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) {
        CommandLineParser c = new DefaultParser();
        Options options = new Options();
        options.addOption(new Option("h", "help", false, "show help text"));
        options.addOption(new Option("p", "port", true, "port of the server"));

        CommandLine cmd = null;
        boolean showHelp = false;
        try {
            cmd = c.parse(options, args);
        } catch (ParseException e) {
            showHelp = true;
            System.out.println("Wrong argument.");
        }

        if (showHelp || cmd.hasOption("h")) {
            new HelpFormatter().printHelp("java -jar server.jar", options);
            System.exit(0);
        }

        int port = Integer.parseInt(cmd.getOptionValue("p", "50000"));
        ;
        new Server(port).run();
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2-server.properties.xml");
    }
}
