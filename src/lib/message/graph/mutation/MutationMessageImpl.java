package lib.message.graph.mutation;

import lib.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;

import static lib.Constants.RECORD_SEPARATOR;

public class MutationMessageImpl extends GraphDbMessage {
    public String key;
    public Json mutations;

    public static final String OPERATION_SEPARATOR = "|";

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

    public static class Builder {
        Json.Builder mutationBuilder = Json.Builder.create();
        String docId;

        private Builder() {}

        public static Builder create(String docId) {
            Builder f = new Builder();
            f.docId = docId;
            return f;
        }

        public MutationMessageImpl finish() {
            return new MutationMessageImpl(docId, mutationBuilder.finish());
        }

        public Builder withReplace(Json.Property prop) {
            mutationBuilder.withProperty(prop.key + OPERATION_SEPARATOR + Operations.REPLACE.name(), prop.value);
            return this;
        }
        public Builder withReplace(String propKey, Json.PropertyValue propVal) {
            mutationBuilder.withProperty(propKey + OPERATION_SEPARATOR + Operations.REPLACE.name(), propVal);
            return this;
        }


    }
}
