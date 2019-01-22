package lib.message.graph;

import lib.Json;
import lib.message.exception.MarshallingException;

import static lib.Constants.RECORD_SEPARATOR;

public class ResponseMessageImpl extends GraphDbMessage {
    public String errorMsg;
    public Json data;

    public ResponseMessageImpl(String errorMsg, Json data) {
        super(GraphMessageType.RESPONSE);
        this.errorMsg = errorMsg;
        this.data = data;
    }

    @Override
    public String marshall() throws MarshallingException {
        return String.join(RECORD_SEPARATOR,
                messageType.name(),
                errorMsg != null ? errorMsg : "",
                data != null ? data.serialize() : "{}"
        );
    }
}
