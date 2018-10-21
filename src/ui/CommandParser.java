package ui;

public class CommandParser {
    public Command parseCommand(String command) {
        String[] tokens = command.split(" ");
        String commandName = tokens.length > 0 ? tokens[0] : "";
        if (commandName.equals("help"))
            return new HelpCommand();
        if (commandName.equals("quit"))
            return new QuitCommand();
        if (commandName.equals("connect"))
            return new ConnectCommand(tokens[1], Integer.valueOf(tokens[2]));
        if (commandName.equals("disconnect"))
            return new DisconnectCommand();
        if (commandName.equals("send"))
            return new SendCommand(tokens[1]);
        return new InvalidCommand();
    }
}
