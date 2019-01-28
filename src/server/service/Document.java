package server.service;

import lib.json.Json;
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
import server.threads.handler.kv.PutHandler;

import java.io.IOException;

import static server.threads.handler.MessageHandlerUtils.isResponsible;

public final class Document {
    public static Json loadJsonDocument(String key, ServerState state) throws DbError, IOException, MarshallingException, KVServerNotFoundException, KeyNotFoundException {
        return Json.deserialize(loadDocument(key, state));
    }

    public static String loadDocument(String key, ServerState state) throws KVServerNotFoundException, KeyNotFoundException, DbError, IOException, MarshallingException {
        if (!isResponsible(state, key, KVMessage.StatusType.GET)) {
            KVMessage getMessage = KvMessageFactory.createGetMessage(key);
            ServerData responsibleServer = state.meta.findRandomResponsibleForGet(key);
            KVMessage response = sendMessage(getMessage, responsibleServer);
            if (!response.isSuccess()) {
                throw new IOException("Could not load data: " + response.getStatus());
            }

            return response.getValue();
        }

        ServerData responsible = state.meta.findKVServerForKey(key);
        KeyValueStore db = state.dbProvider.getDb(responsible.getName());
        return db.get(key);
    }

    public static boolean writeJsonDocument(String key, Json value, ServerState state) throws IOException, MarshallingException, KVServerNotFoundException, DbError {
        return writeDocument(key, value.serialize(), state);
    }

    public static boolean writeDocument(String key, String value, ServerState state) throws KVServerNotFoundException, DbError, IOException, MarshallingException {
        KVMessage putMessage = KvMessageFactory.createPutMessage(key, value);
        KVMessage response;
        if (!isResponsible(state, key, KVMessage.StatusType.PUT)) {
            ServerData responsibleServer = state.meta.findKVServerForKey(key);
            response = sendMessage(putMessage, responsibleServer);
        } else {
            response = new PutHandler().handleRequest(putMessage, state);
        }
        if (!response.isSuccess()) {
            throw new IOException("Could not write data: " + response.getStatus());
        }

        return true;
    }

    private static KVMessage sendMessage(KVMessage getMessage, ServerData responsibleServer) throws IOException, MarshallingException {
        KVMessage response;

        try (Messaging messaging = new Messaging(responsibleServer);) {
            messaging.sendMessage(getMessage);
            response = (KVMessage) messaging.readMessage();
        } catch (Exception e) {
            throw new IOException(e);
        }
        return response;
    }
}
