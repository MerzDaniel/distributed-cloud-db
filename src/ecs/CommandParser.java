package ecs;

import ecs.command.AddServerCommand;
import ecs.command.UsageCommand;

public final class CommandParser {
    public static void parseLine(String line, State state) {

        String[] tokens = line.split(" ");
        Command command = null;

        if (tokens[0].equals("add") && tokens.length == 4) {
            try {
                command = new AddServerCommand(tokens[1], tokens[2], Integer.valueOf(tokens[3]));
            } catch (NumberFormatException e) {}
        }
        if (tokens[0].equals("help")) {
            command = new UsageCommand();
        }

        if (command == null) {
            System.out.println("Unknown Command.");
            new UsageCommand().execute(state);
            return;
        }

        command.execute(state);
    }
}
