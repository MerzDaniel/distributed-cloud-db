package ecs;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Main class for ECSClient
 */
public class Main {

    static final String defaultConfigPath = Paths.get("db", "ecs.config").toString();

    public static void main(String[] args) throws IOException {
        new EcsAdminConsole(defaultConfigPath).start();
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2-ecs.properties.xml");
    }
}
