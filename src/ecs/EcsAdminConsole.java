package ecs;

import ecs.command.LoadConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * The class for admin console where users can perform commands
 */
public class EcsAdminConsole {
    private State state = new State();
    private String configPath;
    private boolean stopRequested = false;
    private Logger logger = LogManager.getLogger(EcsAdminConsole.class);
    private String sshConfigPath;

    /**
     * Constructor to create a new {@link EcsAdminConsole}
     *
     * @param configPath path to the {@code ecs.config} file containing details of {@link server.KVServer}s
     */
    public EcsAdminConsole(String configPath, String sshConfigPath) {
        this.configPath = configPath;
        this.sshConfigPath = sshConfigPath;
    }

    /**
     * Starts the {@link EcsAdminConsole}
     *
     * @throws IOException if any I/O error occurs
     */
    public void start() throws IOException {
        logger.info("Started");

        state.configPath = configPath;
        Properties appProps = new Properties();
        appProps.load(new FileInputStream(sshConfigPath));

        String sshUsername = appProps.getProperty("username");
        if (sshUsername == null) {
            System.out.println("Couldn't find the ssh username in ssh.config");
            System.exit(0);
        }

        state.sshUsername = sshUsername;

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
