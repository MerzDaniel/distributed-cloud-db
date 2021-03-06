package lib.message.graph;

import lib.Constants;
import lib.json.Json;
import lib.message.IMessage;
import lib.message.exception.MarshallingException;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.graph.query.QueryMessageImpl;
import lib.message.graph.query.QueryType;
import lib.message.graph.response.ResponseMessageImpl;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class GraphDbMessage implements IMessage {
    private static Logger logger = LogManager.getLogger(GraphDbMessage.class);

    public static enum GraphMessageType {
        QUERY,
        MUTATION,
        RESPONSE,
    }

    public GraphDbMessage(GraphMessageType type) {
        this.messageType = type;
    }

    public GraphMessageType messageType;

    public static GraphDbMessage deserialize(String message) throws MarshallingException {
        try {
            String split[] = message.split(Constants.RECORD_SEPARATOR);
            switch (GraphMessageType.valueOf(split[0])) {
                case QUERY:
                    return new QueryMessageImpl(QueryType.valueOf(split[1]), split[2], Json.deserialize(split[3]));
                case MUTATION:
                    return new MutationMessageImpl(Json.deserialize(split[1]));
                case RESPONSE:
                    // data
                    if (split[1].length() == 0) return new ResponseMessageImpl(Json.deserialize(split[2]));
                    // error
                    return new ResponseMessageImpl(split[1]);


            }
        } catch (Exception e) {
            throw new MarshallingException(e);
        }

        throw new MarshallingException("Unknown message type");
    }
}
