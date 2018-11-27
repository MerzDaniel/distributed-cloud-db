package lib.message;

/**
 * A message which can be over TCP
 */
public interface IMessage {
    public String marshall() throws MarshallingException;
}
