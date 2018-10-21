package ui;

public class QuitCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        new DisconnectCommand().execute(state);
        state.stopRequested = true;
    }
}
