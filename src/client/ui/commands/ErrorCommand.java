package client.ui.commands;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import client.ui.ApplicationState;
import client.ui.Command;


import static client.ui.Util.writeLine;

public class ErrorCommand implements Command {
    private final Exception ex;
    private final Logger logger = LogManager.getLogger(ErrorCommand.class);

    public ErrorCommand(Exception ex) {
        this.ex = ex;
    }

    @Override
    public void execute(ApplicationState state) {
        logger.error(ex.getMessage());
        logger.error(ex.getStackTrace());

        writeLine("Oh no some unexpected error occured :(");
    }
}
