package ui;

import ui.commands.*;

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
        if (commandName.equals("connect")
                && tokens.length == 3
                && tokens[2].chars().allMatch( Character::isDigit ))
            return new ConnectCommand(tokens[1], Integer.valueOf(tokens[2]));
        if (commandName.equals("disconnect"))
            return new DisconnectCommand(true);
        if (commandName.equals("send")
                && tokens.length > 1) {
            String message = Arrays.asList(tokens).stream().skip(1).collect(Collectors.joining(" "));
            return new SendCommand(message);
        }
        if (commandName.equals("loglevel"))
            return new LogCommand(tokens[1]);
        return new InvalidCommand();
    }
}
