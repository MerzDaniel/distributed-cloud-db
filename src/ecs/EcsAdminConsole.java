package ecs;

import java.io.*;
import java.util.Iterator;

public class EcsAdminConsole {
    private File configPath;
    private boolean stopRequested = false;

    public EcsAdminConsole(String configPath) {
        this.configPath = new File(configPath);
    }

    public void start() {
        loadConfiguration();

        while(!stopRequested) {

        }
        System.out.println("Stopping.");
    }

    private void loadConfiguration() {
        if (!configPath.exists()) {
            System.out.println("Config file does not exist.");
            stopRequested = true;
            return;
        }

        try (BufferedReader reader = new BufferedReader(new  FileReader(configPath))){
            for (Iterator<String> it = reader.lines().iterator(); it.hasNext(); ) {
                String line = it.next();

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
