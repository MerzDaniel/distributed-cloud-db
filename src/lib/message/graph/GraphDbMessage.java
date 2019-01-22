package lib.message.graph;

import lib.Constants;
import lib.Json;
import lib.message.IMessage;
import lib.message.exception.MarshallingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class GraphDbMessage implements IMessage {
    private static Logger logger = LogManager.getLogger(GraphDbMessage.class);

    public static enum GraphMessageType {
        QUERY,
        MUTATION,
    }

    public GraphDbMessage(GraphMessageType type) {
        this.messageType = type;
    }

    public GraphMessageType messageType;

    public static GraphDbMessage deserialize(String message) throws MarshallingException {
        try {
            String splitt[] = message.split(Constants.RECORD_SEPARATOR);
            if (GraphMessageType.QUERY.equals(splitt[0])) {
                return new QueryMessageImpl(Json.deserialize(splitt[1]));
            }
            if (GraphMessageType.MUTATION.equals(splitt[0])) {
                return new MutationMessageImpl(splitt[1], Json.deserialize(splitt[2]));
            }
        } catch (Exception e) {
            throw new MarshallingException(e);
        }

        throw new MarshallingException("Unknown message type");
    }
}
