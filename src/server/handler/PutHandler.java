package server.handler;

import lib.message.KVMessage;
import lib.message.MessageFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.IMessageHandler;
import server.ServerState;
import server.kv.DbError;

public class PutHandler implements IMessageHandler {

    @Override
    public KVMessage handleRequest(KVMessage request, ServerState state) {
        KVMessage response;
        Logger logger = LogManager.getLogger(this.getClass().getName());
        if (shouldDelete(request.getValue())) {
            try {
                state.db.deleteKey(request.getKey());
                response = MessageFactory.createDeleteSuccessMessage();
            } catch (DbError dbError) {
                logger.warn("PUT: Databaseerror while deleting a value", dbError);
                response = MessageFactory.createDeleteErrorMessage();
            }
        } else {
            try {
                boolean updated = state.db.put(request.getKey(), request.getValue());
                if (updated) response = MessageFactory.createPutUpdateMessage();
                else response = MessageFactory.createPutSuccessMessage();
            } catch (DbError dbError) {
                logger.warn("PUT: Databaseerror while PUT a value", dbError);
                response = MessageFactory.createPutErrorMessage();
            }
        }
        return response;
    }

    private boolean shouldDelete(String value) {
        return value == null || value.equals("") || value.equals("null");
    }
}
