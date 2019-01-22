package lib.message.graph.query;

import lib.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;

import static lib.Constants.RECORD_SEPARATOR;

public class QueryMessageImpl extends GraphDbMessage {

    Json request;

    public QueryMessageImpl(Json request) {
        super(GraphMessageType.QUERY);
        this.request = request;
    }

    @Override
    public String marshall() throws MarshallingException {
        return String.join(RECORD_SEPARATOR,
                messageType.name(),
                request != null ? request.serialize() : "{}"
                );
    }
}
