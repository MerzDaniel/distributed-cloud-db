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

    public static class Factory {
        Json.Factory mutationFactory = Json.Factory.create();
        String docId;

        private Factory () {}

        public static Factory create(String docId) {
            Factory f = new Factory();
            f.docId = docId;
            return f;
        }

        public MutationMessageImpl finish() {
            return new MutationMessageImpl(docId, mutationFactory.finish());
        }

        public Factory withReplace(Json.Property prop) {
            mutationFactory.withProperty(prop.key + OPERATION_SEPARATOR + Operations.REPLACE.name(), prop.value);
            return this;
        }
        public Factory withReplace(String propKey, Json.PropertyValue propVal) {
            mutationFactory.withProperty(propKey + OPERATION_SEPARATOR + Operations.REPLACE.name(), propVal);
            return this;
        }


    }
}
