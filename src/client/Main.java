package client;

import client.ui.Application;

/**
 * Main class for the KvStore client
 */
public class Main {
    static {
        System.setProperty("log4j.configurationFile", "log4j2.properties.xml");
    }

    public static void main(String[] args) {
        new Application().run();
    }
}
