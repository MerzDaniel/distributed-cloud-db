package ecs;

import ecs.command.LoadConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The class for admin console where users can perform commands
 */
public class EcsAdminConsole {
    private State state = new State();
    private String configPath;
    private boolean stopRequested = false;
    private Logger logger = LogManager.getLogger(EcsAdminConsole.class);

    /**
     * Constructor to create a new {@link EcsAdminConsole}
     *
     * @param configPath path to the {@code ecs.config} file containing details of {@link server.KVServer}s
     */
    public EcsAdminConsole(String configPath) {
        this.configPath = configPath;
    }

    /**
     * Starts the {@link EcsAdminConsole}
     *
     * @throws IOException if any I/O error occurs
     */
    public void start() throws IOException {
        logger.info("Started");

        System.out.println("Starting ECS client console.");
        new LoadConfiguration(configPath).execute(state);

        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        while (!stopRequested) {
            System.out.print("ECS Admin > ");
            String inputLine = consoleInput.readLine();
            CommandParser.parseLine(inputLine, state);
        }
        System.out.println("Stopping.");
    }

}
