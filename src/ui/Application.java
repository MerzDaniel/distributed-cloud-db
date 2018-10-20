package ui;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Application {
    Logger logger = LogManager.getLogger(Application.class);

    public Application() {
        logger.info("Start application");
        System.out.println("Hello World!");
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2.properties.xml");
    }
    public static void main(String[] args) {
        new Application();
    }
}
