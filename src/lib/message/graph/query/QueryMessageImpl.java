package lib.message.graph.query;

import lib.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.GraphDbMessage;

import static lib.Constants.RECORD_SEPARATOR;

public class QueryMessageImpl extends GraphDbMessage {

    QueryType queryType;
    String queryParam;
    Json request;

    public QueryMessageImpl(QueryType queryType, String queryParam, Json request) {
        super(GraphMessageType.QUERY);
        this.queryType = queryType;
        this.queryParam = queryParam;
        this.request = request;
    }

    @Override
    public String marshall() throws MarshallingException {
        return String.join(RECORD_SEPARATOR,
                messageType.name(),
                queryType.name(),
                queryParam,
                request.serialize()
                );
    }

    public static class Builder {
        String docId;
        Json.Builder queryBuilder = Json.Builder.create();

        private Builder(String docId) {
            this.docId = docId;
        }

        public QueryMessageImpl finish() {
            return new QueryMessageImpl(QueryType.ID, docId, queryBuilder.finish());
        }

        public Builder withProperty(String key) {
            queryBuilder.withProperty(key, Json.UndefinedValue);
            return this;
        }
    }
}
