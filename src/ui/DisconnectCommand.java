package ui;

public class DisconnectCommand implements Command {

    @Override
    public void execute(ApplicationState state) {
        if (state.connection != null) {
            state.connection.disconnect();
            //send the confirmation to the console
        }
    }
}
