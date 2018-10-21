package ui;

public class InvalidCommand implements Command {
    @Override
    public void execute(ApplicationState state) {
        System.out.println("Wrong command.\n");
        System.out.println("Usage:");
        System.out.println("exit : Exit application");
    }
}
