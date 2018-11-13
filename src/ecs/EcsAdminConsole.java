package ecs;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Iterator;

public class EcsAdminConsole {
    private State state = new State();
    private File configPath;
    private boolean stopRequested = false;
    private Logger logger = LogManager.getLogger(EcsAdminConsole.class);

    public EcsAdminConsole(String configPath) {
        this.configPath = new File(configPath);
    }

    public void start() throws IOException {
        logger.info("Started");

        System.out.println("Starting ECS client console.");
        loadConfiguration();

        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        while (!stopRequested) {
            System.out.print("ECS Admin > ");
            String inputLine = consoleInput.readLine();
            CommandParser.parseLine(inputLine, state);
        }
        System.out.println("Stopping.");
    }

    private void loadConfiguration() throws IOException {
        if (!configPath.exists()) {
            logger.debug("Create new config file");
            System.out.println("Config file does not exist. Creating a new one at '" + configPath + "' ...");
            configPath.getParentFile().mkdirs();
            configPath.createNewFile();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            for (Iterator<String> it = reader.lines().iterator(); it.hasNext(); ) {
                String line = it.next();
                String[] tokens = line.split(" ");
                if (tokens.length != 3) {
                    logger.error("Wrong layout in config file: '" + line + "'");
                    System.out.println("Config file layout is wrong. Should be '<name> <host> <port>'");
                    System.exit(0);
                }

                // todo: add new server to the table in the state

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Config loaded.");
    }
}
