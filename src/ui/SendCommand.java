package ui;

public class SendCommand implements Command {
    private String message;

    public SendCommand(String message){
        this.message = message;
    }

    @Override
    public void execute(ApplicationState state) {
        if (state.connection != null) {
            //send the message
            //send the confirmation to the console
        }
    }
}
