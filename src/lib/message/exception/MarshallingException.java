package lib.message.exception;

public class MarshallingException extends Exception {
    public MarshallingException(Exception e) {
        super(e);
    }

    public MarshallingException(String message) {
        super(message);
    }
}
