package client.ui;

import client.store.KVStore;
import lib.metadata.KVStoreMetaData;
import lib.metadata.MetaContent;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import client.ui.commands.ErrorCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * A simple echo client that can establish a connection to the echo server.
 */
public class KVClient {
    Logger logger = LogManager.getLogger(KVClient.class);
    ApplicationState state = new ApplicationState(
            new KVStore(
                    new KVStoreMetaData(Arrays.asList(new MetaContent("127.0.0.1", 50000)))
            )
    );
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
}
