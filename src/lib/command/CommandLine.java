package lib.command;

import org.apache.logging.log4j.Level;
import server.kv.CacheType;

public class CommandLine {
    private int port;
    private int cacheSize;
    private CacheType cacheType;
    private Level logLevel;

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
