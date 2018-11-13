package ecs;

import ecs.command.LoadConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EcsAdminConsole {
    private State state = new State();
    private String configPath;
    private boolean stopRequested = false;
    private Logger logger = LogManager.getLogger(EcsAdminConsole.class);

    public EcsAdminConsole(String configPath) {
        this.configPath = configPath;
    }

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
