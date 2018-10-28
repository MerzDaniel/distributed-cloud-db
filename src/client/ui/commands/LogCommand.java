package client.ui.commands;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import client.ui.ApplicationState;
import client.ui.Command;
import client.ui.Util;

import static client.ui.Util.writeLine;

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
                Util.writeLine("Unsupported log level specified. Please use 'help' to check supported logLevels");
                unsupportedLogLevel = true;
                break;
        }

        if (!unsupportedLogLevel){
            Util.writeLine("current log level is " + logLevel);
        }
    }
}
