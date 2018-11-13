package ecs;

import lib.hash.HashUtil;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;

public class EcsAdminConsole {
    private State state = new State();
    private File configPath;
    private boolean stopRequested = false;
    private Logger logger = LogManager.getLogger(EcsAdminConsole.class);

    public EcsAdminConsole(String configPath) {
        this.configPath = new File(configPath);
    }

    public void start() throws IOException {
        logger.info("Started");

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
            logger.debug("Create new config file");
            System.out.println("Config file does not exist. Creating a new one at '" + configPath + "' ...");
            configPath.getParentFile().mkdirs();
            configPath.createNewFile();
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
                    ServerData s = new ServerData(tokens[0], tokens[1],port,hash);
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
