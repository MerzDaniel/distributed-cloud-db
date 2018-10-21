package ui;

import java.util.Dictionary;

public class CommandParser {
    public Command parseCommand(String command) {
        String[] tokens = command.split(" ");
        String commandName = tokens.length > 0 ? tokens[0] : "";
        if (commandName.equals("help"))
            return new HelpCommand();
        if (commandName.equals("exit"))
            return new ExitCommand();
        if (commandName.equals("connect"))
            return new ConnectCommand(tokens[1], Integer.valueOf(tokens[2]));
        return new InvalidCommand();
    }
}
