package lib.message.graph.response;

import lib.json.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;

import static lib.Constants.RECORD_SEPARATOR;

public class ResponseMessageImpl extends GraphDbMessage {
    public String errorMsg;
    public Json data;

    public ResponseMessageImpl(String errorMsg) {
        super(GraphMessageType.RESPONSE);
        this.errorMsg = errorMsg;
        this.data = null;
    }

    public ResponseMessageImpl(Json data) {
        super(GraphMessageType.RESPONSE);
        this.errorMsg = null;
        this.data = data;
    }

    public ResponseMessageImpl() {
        super(GraphMessageType.RESPONSE);
    }

    public boolean success() {
        return errorMsg == null || errorMsg.equals("");
    }

    public String prettyPrint() {
        String result =  GraphMessageType.RESPONSE.name() + "\n";
        if (errorMsg != null && !errorMsg.equals("")) result += "Errors: " + errorMsg + "\n";
        if (data != null) result += "Data: " + data.prettyPrint();
        return result;
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
