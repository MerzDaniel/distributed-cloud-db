package client.ui;

import client.store.KVStore;
import client.ui.commands.ErrorCommand;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;

/**
 * A simple echo client that can establish a connection to the echo server.
 */
public class KVClient {
    Logger logger;
    ApplicationState state;
    CommandParser commandParser;

    public void run() {
        init();
        logger.info("Start application");
        while (!state.stopRequested) {
            System.out.print("Client> ");
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

    /**
     * Initialize the KVClient
     */
    private void init() {
        logger = LogManager.getLogger(KVClient.class);

        ServerData sd = new ServerData("kitten-" + new Random().nextInt(), "127.0.0.1", 50000);
        state = new ApplicationState(
                new KVStore(
        new KVStoreMetaData(Arrays.asList(sd))
                )
        );
        commandParser = new CommandParser();
    }
}
