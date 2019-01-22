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

    public static class Factory {
        String docId;
        Json.Factory queryFactory = Json.Factory.create();

        private Factory (String docId) {
            this.docId = docId;
        }

        public QueryMessageImpl finish() {
            return new QueryMessageImpl(QueryType.ID, docId, queryFactory.finish());
        }

        public Factory withProperty(String key) {
            queryFactory.withProperty(key, Json.UndefinedValue);
            return this;
        }
    }
}
