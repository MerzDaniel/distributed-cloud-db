package client;

import client.ui.KVClient;

/**
 * Main class for the KVStore client
 */
public class Main {
    static {
        System.setProperty("log4j.configurationFile", "log4j2-client.properties.xml");
    }

    public static void main(String[] args) {
        new KVClient().run();
    }
}
