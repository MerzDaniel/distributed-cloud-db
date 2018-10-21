package ui;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LogCommand implements Command {
    private String logLevel;

    public LogCommand(String logLevel){
        this.logLevel = logLevel;
    }

    @Override
    public void execute(ApplicationState state) {

        switch (logLevel) {
            case "ALL":
                Logger.getRootLogger().setLevel(Level.ALL);
                break;
            case "DEBUG":
                Logger.getRootLogger().setLevel(Level.DEBUG);
                break;
            case "INFO":
                Logger.getRootLogger().setLevel(Level.INFO);
                break;
            case "WARN":
                Logger.getRootLogger().setLevel(Level.WARN);
                break;
            case "ERROR":
                Logger.getRootLogger().setLevel(Level.ERROR);
                break;
            case "FATAL":
                Logger.getRootLogger().setLevel(Level.FATAL);
                break;
            case "OFF":
                Logger.getRootLogger().setLevel(Level.OFF);
                break;
            default:
                Util.writeLine("Unsupported log level specified");
                break;
        }

        Util.writeLine("current log level " + Logger.getRootLogger().getLevel().toString());
    }
}
