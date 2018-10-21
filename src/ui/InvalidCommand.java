package ui;

public class InvalidCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        System.out.println("Wrong command.\n");
        new HelpCommand().execute(state);
    }
}
