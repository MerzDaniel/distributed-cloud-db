package ecs.service;

import ecs.State;
import ecs.command.ConfigureAllCommand;
import lib.message.AdminMessages.FullReplicationMsg;
import lib.message.KVAdminMessage;
import lib.message.MarshallingException;
import lib.message.Messaging;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Comparator;

public final class KvService {
    private static Logger l = LogManager.getLogger(KvService.class);

    public static RunningState getStatus(ServerData sd) throws IOException, MarshallingException {

        Messaging messaging = new Messaging();
        messaging.connect(sd.getHost(), sd.getPort());

        messaging.sendMessage(new KVAdminMessage(KVAdminMessage.StatusType.STATUS));
        KVAdminMessage response = (KVAdminMessage) messaging.readMessage();

        return response.runningState;

    }

    public static KVAdminMessage moveData(ServerData to, Messaging messaging, boolean softMove) throws IOException, MarshallingException {
        KVAdminMessage.StatusType statusType = softMove ? KVAdminMessage.StatusType.MOVE_SOFT : KVAdminMessage.StatusType.MOVE;
        KVAdminMessage msg = new KVAdminMessage(statusType, to);
        messaging.sendMessage(msg);
        return (KVAdminMessage) messaging.readMessage();
    }

    public static KVAdminMessage fullReplicateData(ServerData sourceServer, ServerData dataSrc, ServerData targetServer) throws IOException, MarshallingException {
        Messaging messaging = new Messaging();
        messaging.connect(sourceServer);
        FullReplicationMsg fullReplicationMsg = new FullReplicationMsg(dataSrc.getName(), targetServer.getName());
        messaging.sendMessage(fullReplicationMsg);
        return (KVAdminMessage) messaging.readMessage();
    }

    public static boolean makeReadonly(ServerData sd, Messaging messaging) throws MarshallingException, IOException {
        KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.MAKE_READONLY);
        messaging.sendMessage(msg);
        return ((KVAdminMessage) messaging.readMessage()).status == KVAdminMessage.StatusType.MAKE_SUCCESS;
    }

    public static boolean makeReadonly(ServerData sd) throws MarshallingException, IOException {
        KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.MAKE_READONLY);
        Messaging messaging = new Messaging();
        messaging.connect(sd.getHost(), sd.getPort());
        messaging.sendMessage(msg);
        return ((KVAdminMessage) messaging.readMessage()).status == KVAdminMessage.StatusType.MAKE_SUCCESS;
    }

    public static boolean makeRunning(ServerData sd) throws MarshallingException, IOException {
        KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.START);
        Messaging messaging = new Messaging();
        messaging.connect(sd.getHost(), sd.getPort());
        messaging.sendMessage(msg);
        return ((KVAdminMessage) messaging.readMessage()).status == KVAdminMessage.StatusType.START_SUCCESS;
    }

    public static KVAdminMessage configure(ServerData sd, KVStoreMetaData meta, int index) throws IOException, MarshallingException {
        Messaging messaging = new Messaging();
        messaging.connect(sd.getHost(), sd.getPort());

        KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.CONFIGURE, meta, index);
        messaging.sendMessage(msg);

        KVAdminMessage response = (KVAdminMessage) messaging.readMessage();
        return response;
    }

    public static KVAdminMessage configureAll(KVStoreMetaData storeMetaData) throws IOException, MarshallingException {
        for (int index = 0; index < storeMetaData.getKvServerList().size(); index++) {
            ServerData sd = storeMetaData.getKvServerList().get(index);
            KVAdminMessage response = configure(sd, storeMetaData, index);

            if (response.status.equals(KVAdminMessage.StatusType.CONFIGURE_ERROR)) {
                return response;
            }
        }

        return new KVAdminMessage(KVAdminMessage.StatusType.CONFIGURE_SUCCESS);
    }

    public static boolean removeNode(ServerData removedNode, State state) throws KVServerNotFoundException, IOException, MarshallingException {
        state.storeMeta.getKvServerList().sort(Comparator.comparing(ServerData::getFromHash));
        //get two servers before removed node
        ServerData firstBefore = state.storeMeta.findPreviousKvServer(removedNode);
        ServerData secondBefore = state.storeMeta.findPreviousKvServer(firstBefore);

        //get two servers after the removed node
        ServerData firstAfter = state.storeMeta.findNextKvServer(removedNode);
        ServerData secondAfter = state.storeMeta.findNextKvServer(firstAfter);
        ServerData thirdAfter = state.storeMeta.findNextKvServer(secondAfter);

        //make servers readonly
        KvService.makeReadonly(firstAfter);

        //remove node from metadata
        state.storeMeta.getKvServerList().remove(removedNode);
        //configure all data
        new ConfigureAllCommand().execute(state);

        //logics with firstNodeAfter
        KvService.fullReplicateData(firstAfter, removedNode, firstAfter);
        KvService.makeRunning(firstAfter);
        //update replica servers of first server with new values from removed node
        KvService.fullReplicateData(firstAfter, firstAfter, secondAfter);
        KvService.fullReplicateData(firstAfter, firstAfter, thirdAfter);

        //modify replica servers of nodes after removed node
        KvService.fullReplicateData(firstBefore, firstBefore, secondAfter);
        KvService.fullReplicateData(secondBefore, secondBefore, firstAfter);

        return true;

    }
}
