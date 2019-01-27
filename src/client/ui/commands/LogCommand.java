package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

/**
 * Sets the logLevel
 */
public class LogCommand implements Command {
    private String logLevel;

    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration conf = ctx.getConfiguration();

    public LogCommand(String logLevel){
        this.logLevel = logLevel;
    }

    @Override
    public void execute(ApplicationState state) {

        boolean unsupportedLogLevel = false;

        switch (logLevel) {
            case "ALL":
                conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.ALL);
                ctx.updateLoggers(conf);
                break;
            case "DEBUG":
                conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.DEBUG);
                ctx.updateLoggers(conf);
                break;
            case "INFO":
                conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.INFO);
                ctx.updateLoggers(conf);
                break;
            case "WARN":
                conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.WARN);
                ctx.updateLoggers(conf);
                break;
            case "ERROR":
                conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.ERROR);
                ctx.updateLoggers(conf);
                break;
            case "FATAL":
                conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.FATAL);
                ctx.updateLoggers(conf);
                break;
            case "OFF":
                conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.OFF);
                ctx.updateLoggers(conf);
                break;
            default:
                System.out.println("Unsupported log level specified. Please use 'help' to check supported logLevels");
                unsupportedLogLevel = true;
                break;
        }

        if (!unsupportedLogLevel){
            System.out.println("current log level is " + logLevel);
        }
    }
}
