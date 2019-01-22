package lib.message.graph;

import lib.Json;
import lib.message.exception.MarshallingException;

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
                request.serialize()
                );
    }
}
