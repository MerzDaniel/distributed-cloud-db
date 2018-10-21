package ui;

public class QuitCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        state.stopRequested = true;
    }
}
