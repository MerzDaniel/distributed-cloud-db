package ui;

public class ExitCommand implements Command {

    @Override
    public void Execute(ApplicationState state) {
        state.stopRequested = true;
    }
}
