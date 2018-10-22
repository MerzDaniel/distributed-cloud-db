package ui;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ui.commands.ErrorCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A simple echo client that can establish a connection to the echo server.
 */
public class Application {
    Logger logger = LogManager.getLogger(Application.class);
    ApplicationState state = new ApplicationState();
    CommandParser commandParser = new CommandParser();

    public void run() {
        logger.info("Start application");
        while (!state.stopRequested) {
            System.out.print("EchoClient> ");
            processInput();
        }
        logger.info("Stop application");
    }

    /**
     * Reads a line form System.in and calls CommandParser to parse the content. The resulting command will be executed.
     */
    private void processInput() {
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        String command = "";
        try {
            command = consoleInput.readLine();
        } catch (IOException e) {
            logger.warn("EXCEPTION: " + e.getMessage());
            logger.warn(e.getStackTrace());
        }
        try {
            Command c = commandParser.parseCommand(command);
            c.execute(state);
        } catch (Exception ex) {
            new ErrorCommand(ex).execute(state);
        }
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2.properties.xml");
    }

    public static void main(String[] args) {
        new Application().run();
    }
}
