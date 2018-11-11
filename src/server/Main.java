package server;

import server.command.CommandLine;
import server.command.CommandParser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import server.kv.CacheType;

/**
 * The main class of the KVServer which has the main method to start a server for a given port
 */
public class Main {

    public static void main(String[] args) {
        CommandLine cm = new CommandParser().parse(args);

        int port = cm.getPort();
        int cacheSize = cm.getCacheSize();
        CacheType cacheType = cm.getCacheType();
        Level logLevel = cm.getLogLevel();

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();
        conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(logLevel);
        ctx.updateLoggers(conf);


        String serverStartMsg = String.format(
                "Starting server on port %d (cache: %s cache-size: %d log-level: %s)",
                cm.getPort(), cm.getCacheType().toString(), cm.getCacheSize(), cm.getLogLevel().toString()
        );
        LogManager.getLogger(Main.class).info(serverStartMsg);
        System.out.println(serverStartMsg);

        new KVServer("localhost", port, cacheSize, cacheType).run();
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2-server.properties.xml");
    }
}
