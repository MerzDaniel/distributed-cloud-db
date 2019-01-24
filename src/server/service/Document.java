package server.service;

import lib.message.IMessage;
import lib.message.Messaging;
import lib.message.exception.MarshallingException;
import lib.message.kv.KVMessage;
import lib.message.kv.KvMessageFactory;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import server.ServerState;
import server.kv.DbError;
import server.kv.KeyNotFoundException;
import server.kv.KeyValueStore;

import java.io.IOException;

import static server.threads.handler.MessageHandlerUtils.isResponsible;

public class Document {
    public static String loadDocument(String key, ServerState state) throws KVServerNotFoundException, KeyNotFoundException, DbError, IOException, MarshallingException {
        if (!isResponsible(state, key, KVMessage.StatusType.GET)) {
            KVMessage getMessage = KvMessageFactory.createGetMessage(key);
            ServerData responsibleServer = state.meta.findRandomResponsibleForGet(key);
            Messaging messaging = new Messaging(responsibleServer);
            messaging.sendMessage(getMessage);

            KVMessage response = (KVMessage) messaging.readMessage();
            if (!response.isSuccess()) {
                throw new IOException("Could not load data: " + response.getStatus());
            }

            return response.getValue();
        }

        ServerData responsible = state.meta.findKVServerForKey(key);
        KeyValueStore db = state.dbProvider.getDb(responsible.getName());
        return db.get(key);
    }
}
