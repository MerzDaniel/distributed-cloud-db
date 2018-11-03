package client.ui;

import client.ui.commands.*;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The CommandParser parses input from the user and returns the correct Command to execute
 */
public class CommandParser {
    public Command parseCommand(String command) {
        String[] tokens = command.split(" ");
        String commandName = tokens.length > 0 ? tokens[0].toLowerCase() : "";
        if (commandName.equals("help"))
            return new HelpCommand();
        if (commandName.equals("quit"))
            return new QuitCommand();
        if (commandName.equals("get") && tokens.length == 2)
            return new GetCommand(tokens[1]);
        if (commandName.equals("put") && tokens.length >= 2)
            return new PutCommand(tokens[1], Arrays.asList(tokens).subList(2, tokens.length).stream().collect(Collectors.joining(" ")));
        if (commandName.equals("connect")
                && tokens.length == 3
                && tokens[2].chars().allMatch( Character::isDigit ))
            return new ConnectCommand(tokens[1], Integer.valueOf(tokens[2]));
        if (commandName.equals("c"))
            return new ConnectCommand("localhost", 50000);
        if (commandName.equals("disconnect"))
            return new DisconnectCommand();
        if (commandName.equals("loglevel"))
            return new LogCommand(tokens[1]);
        return new InvalidCommand();
    }
}
