package ecs;

import ecs.command.*;

public final class CommandParser {
    public static void parseLine(String line, State state) {

        String[] tokens = line.split(" ");
        Command command = null;

        if (tokens[0].equals("add") && tokens.length == 4) {
            try {
                command = new AddServerCommand(tokens[1], tokens[2], Integer.valueOf(tokens[3]));
            } catch (NumberFormatException e) {}
        }
        if (tokens[0].equals("c"))
            command = new SshCommand();

        else if (tokens[0].equals("cs")) {
            new ConfigureAllCommand().execute(state);
            command = new StartServersCommand();
        }

        else if (tokens[0].equals("help"))
            command = new UsageCommand();

        else if (tokens[0].equals("start"))
            command = new StartServersCommand();

        else if (tokens[0].equals("configure"))
            command = new ConfigureAllCommand();

        else if (tokens[0].equals("status"))
            command = new ServerStatusCommand();

        else if (tokens[0].equals("remove") && tokens.length == 2)
            command = new RemoveNodeCommand(tokens[1]);

        if (command == null) {
            System.out.println("Unknown Command.");
            new UsageCommand().execute(state);
            return;
        }

        command.execute(state);
    }
}
