package client;

import client.ui.Application;

public class Main {
    static {
        System.setProperty("log4j.configurationFile", "log4j2.properties.xml");
    }

    public static void main(String[] args) {
        new Application().run();
    }
}
