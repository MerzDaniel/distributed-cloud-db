package lib.message.graph.mutation;

import lib.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;

import static lib.Constants.RECORD_SEPARATOR;

public class MutationMessageImpl extends GraphDbMessage {
    public String key;
    public Json mutations;

    public MutationMessageImpl(String key, Json mutations) {
        super(GraphMessageType.MUTATION);
        this.key = key;
        this.mutations = mutations;
    }

    @Override
    public String marshall() throws MarshallingException {
        return String.join(RECORD_SEPARATOR,
                messageType.name(),
                key != null ? key : "",
                mutations != null ? mutations.serialize() : "{}"
        );
    }
}
