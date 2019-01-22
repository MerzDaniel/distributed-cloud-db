package lib.message;

import lib.message.exception.MarshallingException;

/**
 * A message which can be over TCP
 */
public interface IMessage {
    public String marshall() throws MarshallingException;
}
