package ecs.command;

import ecs.Command;
import ecs.State;
import lib.hash.HashUtil;
import lib.metadata.ServerData;
import lib.server.CacheType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class loads the configuration data to the {@link server.KVServer} instances
 */
public class LoadConfiguration implements Command {
    File configPath;
    Logger logger = LogManager.getLogger(LoadConfiguration.class);

    /**
     * Constructor to create an instance of {@link LoadConfiguration}
     *
     * @param configPath path to the configuration file
     */
    public LoadConfiguration(String configPath) {
        this.configPath = new File(configPath);
    }

    /**
     * Execute the commamnd
     *
     * @param state state
     */
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
                if (tokens.length == 0 || tokens[0].equals("") || tokens[0].equals("#") || tokens[0].startsWith("#") )
                    continue;
                if (tokens.length < 3 || tokens.length == 4 || tokens.length > 5) {
                    logger.error("Wrong layout in config file: '" + line + "'");
                    System.out.println("Wrong line in the config file (name,host,port,cachetype,cachesize): " + line);
                    continue;
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
                    CacheType type = CacheType.LFU;
                    int cacheSize = 1000;
                    if (tokens.length > 3) {
                        type = CacheType.valueOf(tokens[3]);
                        cacheSize = Integer.parseInt(tokens[4]);
                    }
                    BigInteger hash = HashUtil.getHash(name);
                    ServerData s = new ServerData(name, host, port, hash);
                    s.setCacheType(type); s.setCacheSize(cacheSize);

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
