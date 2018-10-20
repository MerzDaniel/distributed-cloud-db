package ui;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Application {
    Logger logger = LogManager.getLogger(Application.class);
    boolean stopRequested = false;

    public Application() {
        logger.info("Start application");
        while (true) {
            System.out.print("EchoClient> ");
            processInput();
            if (stopRequested)
                break;
        }
        logger.info("Stop application");
    }

    private void processInput() {
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        String command;
        try {
            command = consoleInput.readLine();
        } catch (IOException e) {
            logger.warn("EXCEPTION: " + e.getMessage());
            logger.warn(e.getStackTrace());
        }
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2.properties.xml");
    }

    public static void main(String[] args) {
        new Application();
    }
}
