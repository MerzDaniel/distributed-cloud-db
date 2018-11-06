package ecs;

import java.io.*;
import java.util.Iterator;

public class EcsAdminConsole {
    private State state = new State();
    private File configPath;
    private boolean stopRequested = false;

    public EcsAdminConsole(String configPath) {
        this.configPath = new File(configPath);
    }

    public void start() throws IOException {
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
            System.out.println("Config file does not exist. Creating a new one at '" + configPath + "' ...");
            configPath.getParentFile().mkdirs();
            configPath.createNewFile();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            for (Iterator<String> it = reader.lines().iterator(); it.hasNext(); ) {
                String line = it.next();

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Config loaded.");
    }
}
