package ecs.command;

import ecs.Command;
import ecs.State;
import lib.hash.HashUtil;
import lib.metadata.ServerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;

public class LoadConfiguration implements Command {
    File configPath;
    Logger logger = LogManager.getLogger(LoadConfiguration.class);

    public LoadConfiguration(String configPath) {
        this.configPath = new File(configPath);
    }

    @Override
    public void execute(State state) {
        if (!configPath.exists()) {
            System.out.println("Config was not found at " + configPath.getAbsolutePath());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            LinkedList<ServerData> servers = new LinkedList<>();
            for (Iterator<String> it = reader.lines().iterator(); it.hasNext(); ) {
                String line = it.next();
                String[] tokens = line.split(" ");
                if (tokens.length != 3) {
                    logger.error("Wrong layout in config file: '" + line + "'");
                    System.out.println("Config file layout is wrong. Should be '<name> <host> <port>'");
                    System.exit(0);
                }

                boolean nameExists = servers.stream().anyMatch(s -> s.getName().equals(tokens[0]));
                if (nameExists) {
                    System.out.println("Could not load configuration a name exists multiple times");
                    return;
                }
                try {
                    String name = tokens[0];
                    String host = tokens[1];
                    int port = Integer.parseInt(tokens[2]);
                    BigInteger hash = HashUtil.getHash(name);
                    ServerData s = new ServerData(name, host, port, hash);
                    servers.add(s);
                } catch (Exception e) {
                    System.out.println("Config file contains errors!");
                    return;
                }

                state.meta.getKvServerList().clear();
                state.meta.getKvServerList().addAll(servers);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Config loaded.");
    }
}
