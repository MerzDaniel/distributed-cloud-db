package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static client.ui.Util.writeLine;

/**
 * Used to print "unexpected error message" and write the log with the error state.
 */
public class ErrorCommand implements Command {
    private final Exception ex;
    private final Logger logger = LogManager.getLogger(ErrorCommand.class);

    public ErrorCommand(Exception ex) {
        this.ex = ex;
    }

    @Override
    public void execute(ApplicationState state) {
        logger.error(ex.getMessage(), ex);

        writeLine("Oh no some unexpected error occured :( " + ex.getMessage());
    }
}
