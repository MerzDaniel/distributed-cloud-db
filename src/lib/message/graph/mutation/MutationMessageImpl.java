package lib.message.graph.mutation;

import lib.json.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static lib.Constants.RECORD_SEPARATOR;

public class MutationMessageImpl extends GraphDbMessage {
    public Json mutations;

    public static final String OPERATION_SEPARATOR = "|";

    public MutationMessageImpl(Json mutations) {
        super(GraphMessageType.MUTATION);
        this.mutations = mutations;
    }

    @Override
    public String marshall() throws MarshallingException {
        return String.join(RECORD_SEPARATOR,
                messageType.name(),
                mutations != null ? mutations.serialize() : "{}"
        );
    }

    public String prettyPrint() {
        return GraphMessageType.MUTATION.name() + " " + mutations.prettyPrint();
    }

    public static class Builder {
        HashMap<String, Json.Builder> docMutationBuilders = new HashMap<>();

        private Builder() {
        }

        public static Builder create() {
            Builder f = new Builder();
            return f;
        }

        public MutationMessageImpl finish() {
            Json.Builder combinedMutationsBuilder = Json.Builder.create();
            for (String docId : docMutationBuilders.keySet()) {
                combinedMutationsBuilder.withJsonProperty(docId, getMutationBuilder(docId).finish());
            }
            return new MutationMessageImpl(combinedMutationsBuilder.finish());
        }

        public Builder withReplace(String docId, Json.Property prop) {
            getMutationBuilder(docId).withProperty(prop.key + OPERATION_SEPARATOR + Operations.REPLACE.name(), prop.value);
            return this;
        }

        public Builder withReplace(String docId, String propKey, String val) {
            return withReplace(docId, propKey, new Json.StringValue(val));
        }

        public Builder withReplace(String docId, String propKey, Json.PropertyValue propVal) {
            getMutationBuilder(docId).withProperty(propKey + OPERATION_SEPARATOR + Operations.REPLACE.name(), propVal);
            return this;
        }

        public Builder withMerge(String docId, String propKey, String val) {
            return withMerge(docId, propKey, new Json.StringValue(val));
        }

        public Builder withMerge(String id, String toUser, List<String> users) {
            return withMerge(id, toUser, new Json.ArrayValue(
                            users.stream()
                                    .map(u -> new Json.StringValue(u))
                                    .collect(Collectors.toList())
                    )
            );
        }

        public Builder withMerge(String docId, String propKey, Json.PropertyValue propVal) {
            getMutationBuilder(docId).withProperty(propKey + OPERATION_SEPARATOR + Operations.MERGE.name(), propVal);
            return this;
        }

        public Builder withStringArrayMerge(String docId, String propKey, List<String> list) {
            getMutationBuilder(docId).withStringArrayProperty(propKey + OPERATION_SEPARATOR + Operations.MERGE.name(), list);
            return this;
        }

        private Json.Builder getMutationBuilder(String docId) {
            if (docMutationBuilders.get(docId) == null) docMutationBuilders.put(docId, Json.Builder.create());
            return docMutationBuilders.get(docId);
        }

    }
}
