package ecs.service;

import lib.message.AdminMessages.FullReplicationMsg;
import lib.message.KVAdminMessage;
import lib.message.MarshallingException;
import lib.message.Messaging;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

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
}
