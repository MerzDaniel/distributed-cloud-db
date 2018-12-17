package server.threads.handler.kv;

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

        KeyValueStore db = state.dbProvider.getDb(state.currentServerServerData);
        if (shouldDelete(request.getValue())) {
            try {
                db.deleteKey(request.getKey());
                response = MessageFactory.createDeleteSuccessMessage();
                this.deleteFromReplicas(request, state);
            } catch (DbError dbError) {
                logger.warn("PUT: Databaseerror while deleting a value", dbError);
                response = MessageFactory.createDeleteErrorMessage();
            }
        } else {
            try {
                boolean updated = db.put(request.getKey(), request.getValue());
                if (updated) response = MessageFactory.createPutUpdateMessage();
                else response = MessageFactory.createPutSuccessMessage();
                this.putToReplicas(request, state);
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

    private KVAdminMessage putToReplica(KVMessage request, ServerData serverData) {
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

    private KVAdminMessage putToReplicas(KVMessage request, ServerState state) {
        List<ServerData> replicaServers;

        try {
            replicaServers = state.meta.findReplicaKVServers(request.getKey());
        } catch (KVServerNotFoundException e) {
            return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_ERROR);
        }

        for (ServerData serverData : replicaServers) {
            CompletableFuture.runAsync(() -> {
                this.putToReplica(request, serverData);
            });
        }
        return new KVAdminMessage(KVAdminMessage.StatusType.PUT_REPLICATE_SUCCESS);
    }

    private KVAdminMessage deleteFromReplica(KVMessage request, ServerData serverData) {
        Messaging con;
        KVAdminMessage response;

        con = new Messaging();
        try {
            con.connect(serverData.getHost(), serverData.getPort());
            KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.DELETE_REPLICATE, request.getKey(), request.getValue());
            con.sendMessage(msg);
            response = (KVAdminMessage) con.readMessage();
        } catch (IOException | MarshallingException e) {
            logger.warn("An error occurred while replicating data ", e);
            return new KVAdminMessage(KVAdminMessage.StatusType.DELETE_REPLICATE_ERROR);
        }

        return response;
    }

    private KVAdminMessage deleteFromReplicas(KVMessage request, ServerState state) {
        List<ServerData> replicaServers;

        try {
            replicaServers = state.meta.findReplicaKVServers(request.getKey());
        } catch (KVServerNotFoundException e) {
            return new KVAdminMessage(KVAdminMessage.StatusType.DELETE_REPLICATE_ERROR);
        }

        for (ServerData serverData : replicaServers) {
            CompletableFuture.runAsync(() -> {
                this.deleteFromReplica(request, serverData);
            });
        }
        return new KVAdminMessage(KVAdminMessage.StatusType.DELETE_REPLICATE_SUCCESS);
    }
}

