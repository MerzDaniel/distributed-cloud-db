package ecs;

import ecs.command.*;
import lib.server.CacheType;

/**
 * This class parse the user commands from {@link EcsAdminConsole} to {@link Command} instances
 */
public final class CommandParser {

    /**
     * Parse the command
     *
     * @param line  command string
     * @param state state
     */
    public static void parseLine(String line, State state) {

        String[] tokens = line.split(" ");
        Command command = null;

        if (tokens[0].equals("cs")) {
            new ConfigureAllCommand().execute(state);
            command = new StartServersCommand();
        } else if (tokens[0].equals("startup"))
            command = new StartupServers();

        else if (tokens[0].equals("help"))
            command = new UsageCommand();

        else if (tokens[0].equals("start"))
            command = new StartServersCommand();

        else if (tokens[0].equals("configure"))
            command = new ConfigureAllCommand();

        else if (tokens[0].equals("status"))
            command = new ServerStatusCommand();

        else if (tokens[0].equals("gstatus"))
            command = new GossipServerStatusCommand();

        else if (tokens[0].equals("remove")) {
            if (tokens.length > 1)
                command = new RemoveNodeCommand(tokens[1]);
            else
                command = new RemoveNodeCommand();

        } else if (tokens[0].equals("shutdown"))
            command = new ShutdownCommand();

        else if (tokens[0].equals("stop"))
            command = new StopServersCommand();

        else if (tokens[0].equals("add") && tokens.length == 3) {
            try {
                command = new AddServerCommand(Integer.parseInt(tokens[1]), CacheType.valueOf(tokens[2]));
            } catch (Exception e) {
            }

        } else if (tokens[0].equals("failover"))
            command = new FailOverCommand();
        else if (tokens[0].equals("load"))
            command = new LoadConfiguration();

        else if (tokens[0].

                equals("init") && (tokens.length == 4 || tokens.length == 2))
            try {
                int cacheSize;
                CacheType cacheType;
                if (tokens.length == 2) {
                    cacheSize = 100;
                    cacheType = CacheType.LRU;
                } else {
                    cacheSize = Integer.parseInt(tokens[2]);
                    cacheType = CacheType.valueOf(tokens[3]);
                }
                command = new InitCommand(Integer.parseInt(tokens[1]), cacheSize, cacheType);
            } catch (
                    Exception e) {
            }

        else if (tokens[0].equals("fillupdb") && tokens.length == 2)
            command = new

                    FillupDbCommand(Integer.parseInt(tokens[1]));

        if (command == null) {
            System.out.println("Unknown Command.");
            new UsageCommand().execute(state);
            return;
        }

        command.execute(state);
    }
}
