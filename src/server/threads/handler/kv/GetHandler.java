package server.threads.handler.kv;

import lib.message.kv.KVMessage;
import lib.message.kv.MessageFactory;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.IMessageHandler;
import server.ServerState;
import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.kv.KeyValueStore;

public class GetHandler implements IMessageHandler {
    Logger logger = LogManager.getLogger(GetHandler.class);

    @Override
    public KVMessage handleRequest(KVMessage kvMessage, ServerState state) {
        KVMessage response;

        try {
            ServerData responsible = state.meta.findKVServerForKey(kvMessage.getKey());
            KeyValueStore db = state.dbProvider.getDb(responsible.getName());
            String value = db.get(kvMessage.getKey());
            response = MessageFactory.createGetSuccessMessage(kvMessage.getKey(), value);
        } catch (KeyNotFoundException e) {
            logger.info(String.format("Key '%s' not found", kvMessage.getKey()));
            response = MessageFactory.createGetNotFoundMessage();
        } catch (DbError e) {
            logger.warn("Some error occured at database level", e);
            response = MessageFactory.createGetErrorMessage();
        } catch (KVServerNotFoundException e) {
            logger.warn("KvServer not found? should not happen");
            response = MessageFactory.createGetErrorMessage();
        }

        return response;
    }
}
