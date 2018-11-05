package server.command;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import server.kv.CacheType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class contains the methods to parse a command line arguments during the application startup
 */
public class CommandParser {

    /**
     * Supported commandOptions
     */
    public enum CommandOption {

        PORT("--port"),
        CACHE_TYPE("--cache-type"),
        CACHE_SIZE("--cache-size"),
        LOG_LEVEL("--log-level"),
        HELP("--help");

        private String commandValue;

        CommandOption(String commandValue){
            this.commandValue = commandValue;
        }

        public String getCommandValue() {
            return this.commandValue;
        }
    }

    final Logger logger = LogManager.getLogger(CommandParser.class);

    /**
     * Parse the command line {@code arg} array to a {@link CommandLine}
     *
     * @param args command line arguments array
     * @return a {@link CommandLine} object
     */
    public CommandLine parse(String[] args) {

        int port = 50000;
        int cacheSize = 10;
        CacheType cacheType = CacheType.NONE;
        Level logLevel = Level.ALL;

        List<String> argsList = Arrays.asList(args);

        List<String> optionsList = IntStream.range(0, argsList.size())
                .filter(n -> n % 2 == 0)
                .mapToObj(argsList::get)
                .collect(Collectors.toList());

        List<String> valueList = IntStream.range(0, argsList.size())
                .filter(n -> n % 2 != 0)
                .mapToObj(argsList::get)
                .collect(Collectors.toList());

        try {
            if (optionsList.contains(CommandOption.HELP.commandValue) || valueList.size() != optionsList.size() || hasUnrecognizedOption(optionsList)) {
                this.printHelp();
                System.exit(0);
            }
            if (optionsList.contains(CommandOption.PORT.commandValue)) {
                port = Integer.valueOf(valueList.get(optionsList.indexOf(CommandOption.PORT.commandValue)));
            }
            if (optionsList.contains(CommandOption.CACHE_TYPE.commandValue)) {
                cacheType = CacheType.valueOf(valueList.get(optionsList.indexOf(CommandOption.CACHE_TYPE.commandValue)));
            }
            if (optionsList.contains(CommandOption.CACHE_SIZE.commandValue)) {
                cacheSize = Integer.valueOf(valueList.get(optionsList.indexOf(CommandOption.CACHE_SIZE.commandValue)));
            }
            if (optionsList.contains(CommandOption.LOG_LEVEL.commandValue)) {
                logLevel = Level.valueOf(valueList.get(optionsList.indexOf(CommandOption.LOG_LEVEL.commandValue)));
            }
        } catch (Exception ex) {
            this.printHelp();
            System.exit(0);
        }

        return new CommandLine(port, cacheSize, cacheType, logLevel);
    }

    private void printHelp() {
        System.out.println("An error occurred parsing command arguments");
        System.out.println("Usage of command arguments:");
        System.out.println("--port <port>           : specify the port to connect");
        System.out.println("--cache-type <type>     : allowed values are FIFO, LFU, LRU and NONE");
        System.out.println("--cache-size <size>     : set the size of the cache");
        System.out.println("--log-level <level>     : Supported logLevels are ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF");
        System.out.println("--help                  : print help");
    }

    private boolean hasUnrecognizedOption(List<String> optionsList) {
        List<String> allCommandOptions = Arrays.stream(CommandOption.values()).map(CommandOption::getCommandValue).collect(Collectors.toList());
        return optionsList.stream().anyMatch(option -> !allCommandOptions.contains(option));
    }

}
