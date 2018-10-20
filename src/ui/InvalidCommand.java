package ui;

public class InvalidCommand implements Command {

    @Override
    public void Execute(ApplicationState state) {
        System.out.println("Invalid Command");
    }
}
