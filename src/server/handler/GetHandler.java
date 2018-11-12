package server.handler;

import lib.message.KVMessage;
import lib.message.MessageFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.IMessageHandler;
import server.ServerState;
import server.kv.DbError;
import server.kv.KeyNotFoundException;

public class GetHandler implements IMessageHandler {
    Logger logger = LogManager.getLogger(GetHandler.class);

    @Override
    public KVMessage handleRequest(KVMessage kvMessage, ServerState state) {
        KVMessage response;

        try {
            String value = state.db.get(kvMessage.getKey());
            response = MessageFactory.createGetSuccessMessage(kvMessage.getKey(), value);
        } catch (KeyNotFoundException e) {
            logger.info(String.format("Key '%s' not found", kvMessage.getKey()));
            response = MessageFactory.createGetNotFoundMessage();
        } catch (DbError e) {
            logger.warn("Some error occured at database level", e);
            response = MessageFactory.createGetErrorMessage();
        }
        return response;
    }
}