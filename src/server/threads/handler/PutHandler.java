package server.threads.handler;

import lib.message.*;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import server.IMessageHandler;
import server.ServerState;
import server.kv.DbError;
import server.kv.KeyValueStore;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PutHandler implements IMessageHandler {
    Logger logger = LogManager.getLogger(this.getClass().getName());

    @Override
    public KVMessage handleRequest(KVMessage request, ServerState state) {
        KVMessage response;

        KeyValueStore db;

        if (shouldDelete(request.getValue())) {
            try {
                db = MessageHandlerUtils.getDatabase(state, request.getKey());
                db.deleteKey(request.getKey());
                response = MessageFactory.createDeleteSuccessMessage();
            } catch (DbError dbError) {
                logger.warn("PUT: Databaseerror while deleting a value", dbError);
                response = MessageFactory.createDeleteErrorMessage();
            } catch (NoKeyValueStoreException e) {
                logger.warn(String.format("Couldn't find the responsible database for key '%s'", request.getKey()));
                response = MessageFactory.createDeleteErrorMessage();
            }
        } else {
            try {
                db = MessageHandlerUtils.getDatabase(state, request.getKey());
                boolean updated = db.put(request.getKey(), request.getValue());
                if (updated) response = MessageFactory.createPutUpdateMessage();
                else response = MessageFactory.createPutSuccessMessage();
            } catch (DbError dbError) {
                logger.warn("PUT: Databaseerror while PUT a value", dbError);
                response = MessageFactory.createPutErrorMessage();
            } catch (NoKeyValueStoreException e) {
                logger.warn(String.format("Couldn't find the responsible database for key '%s'", request.getKey()));
                response = MessageFactory.createPutErrorMessage();
            }
        }

        if(MessageHandlerUtils.isResponsibleCoordinator(state, request.getKey())) {
            this.sendToReplicas(request, state);
        }

        return response;
    }

    private boolean shouldDelete(String value) {
        return value == null || value.equals("") || value.equals("null");
    }

    private KVAdminMessage sendToReplica(KVMessage request, ServerData serverData) {
        Messaging con;
        KVAdminMessage response;

        con = new Messaging();
        try {
            con.connect(serverData.getHost(), serverData.getPort());
            KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE, request.getKey(), request.getValue());
            con.sendMessage(msg);
            response = (KVAdminMessage) con.readMessage();
        } catch (IOException | MarshallingException e) {
            logger.warn("An error occurred while replicating data ", e);
            return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_ERROR);
        }

        return response;
    }

    private KVAdminMessage sendToReplicas(KVMessage request, ServerState state) {
        List<ServerData> replicaServers;

        try {
            replicaServers = state.meta.findReplicaKVServers(request.getKey());
        } catch (KVServerNotFoundException e) {
            return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_ERROR);
        }

        for (ServerData serverData : replicaServers) {
            CompletableFuture.runAsync(() -> {
                this.sendToReplica(request, serverData);
            });
        }
        return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_SUCCESS);
    }
}

