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
        //update replica servers of the firstAfter node (it's coordinator database has been updated)
        KvService.fullReplicateData(firstAfter, firstAfter, secondAfter);
        KvService.fullReplicateData(firstAfter, firstAfter, thirdAfter);

        //modify replica servers for servers lie before removedNode ( rmeovedNode is no longer there. So they both have a new replica server)
        KvService.fullReplicateData(firstBefore, firstBefore, secondAfter);
        KvService.fullReplicateData(secondBefore, secondBefore, firstAfter);

        return true;

    }

    public static boolean addNode(ServerData addedNode, State state) throws KVServerNotFoundException, IOException, MarshallingException {
        state.storeMeta.getKvServerList().sort(Comparator.comparing(ServerData::getFromHash));
        //get two servers before added  node
        ServerData firstBefore = state.storeMeta.findPreviousKvServer(addedNode);
        ServerData secondBefore = state.storeMeta.findPreviousKvServer(firstBefore);

        //get two servers after the added node
        ServerData firstAfter = state.storeMeta.findNextKvServer(addedNode);
        ServerData secondAfter = state.storeMeta.findNextKvServer(firstAfter);

        //make servers readonly
        KvService.makeReadonly(addedNode);
        KvService.makeReadonly(firstAfter);

        //remove node from metadata
        state.storeMeta.getKvServerList().add(addedNode);
        //configure all data
        new ConfigureAllCommand().execute(state);

        //logics with firstNodeAfter
        Messaging influencedServerCon = new Messaging();
        influencedServerCon.connect(firstAfter.getHost(), firstAfter.getPort());
        KvService.moveData(addedNode, influencedServerCon, false);
        KvService.fullReplicateData(firstAfter, addedNode, firstAfter);
        KvService.makeRunning(addedNode);
        //update replica servers of newly added server
        KvService.fullReplicateData(addedNode, addedNode, firstAfter);
        KvService.fullReplicateData(addedNode, addedNode, secondAfter);

        //update replica servers of nodes before newly added node
        KvService.fullReplicateData(firstBefore, firstBefore, addedNode);
        KvService.fullReplicateData(secondBefore, secondBefore, addedNode);

        return true;

    }

    public static boolean configureAll(KVStoreMetaData meta) {
        boolean universeIsOk = true;
        for (int index = 0; index < meta.getKvServerList().size(); index++) {
            ServerData sd = meta.getKvServerList().get(index);

            try {
                KVAdminMessage response = configure(sd, meta, index);
                if (!response.status.equals(KVAdminMessage.StatusType.CONFIGURE_SUCCESS)) {
                    universeIsOk = false;
                    System.out.println(String.format("Error while configuring the server %s:%d : %s", sd.getHost(), sd.getPort(), response.status));
                }
            } catch (IOException | MarshallingException e) {
                universeIsOk = false;
                System.out.println(String.format("Error while configuring the server %s:%d : %s", sd.getHost(), sd.getPort(), e.getMessage()));
            }
        }
        return universeIsOk;
    }
}
