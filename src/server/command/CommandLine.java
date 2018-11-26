package server.command;

import org.apache.logging.log4j.Level;
import lib.server.CacheType;

/**
 * The class repersents a command line arguments passed to the application startup
 */
public class CommandLine {
    private int port;
    private int cacheSize;
    private CacheType cacheType;
    private Level logLevel;

    /**
     * Create a new CommandLine
     *
     * @param port the port passed with command arguments
     * @param cacheSize the cacheSize passed with command arguments
     * @param cacheType the cacheTypec passed with command arguments
     * @param logLevel the logLevel passed with command arguments
     */
    public CommandLine(int port, int cacheSize, CacheType cacheType, Level logLevel) {
        this.port = port;
        this.cacheSize = cacheSize;
        this.cacheType = cacheType;
        this.logLevel = logLevel;
    }

    public int getPort() {
        return port;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public Level getLogLevel() {
        return logLevel;
    }
}
