package ecs.service;

import lib.SocketUtil;
import lib.communication.Connection;
import lib.message.*;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class KvService {
    private static Logger l = LogManager.getLogger(KvService.class);

    public static RunningState getStatus(ServerData sd) throws IOException, MarshallingException {

        try (Socket s = new Socket(sd.getHost(), sd.getPort());
             InputStream i = s.getInputStream();
             OutputStream o = s.getOutputStream()) {
            MessageMarshaller.unmarshall(SocketUtil.readMessage(i));

            SocketUtil.sendMessage(o, MessageMarshaller.marshall(new KVAdminMessage(KVAdminMessage.StatusType.STATUS)));
            KVAdminMessage response = (KVAdminMessage) MessageMarshaller.unmarshall(SocketUtil.readMessage(i));

            return response.runningState;
        }

    }
    public static KVAdminMessage moveData(ServerData to, Messaging messaging, boolean softMove) throws IOException, MarshallingException {
        KVAdminMessage.StatusType statusType = softMove ? KVAdminMessage.StatusType.MOVE_SOFT : KVAdminMessage.StatusType.MOVE;
        KVAdminMessage msg = new KVAdminMessage(statusType, to);
        messaging.sendMessage(msg);
        return (KVAdminMessage)messaging.readMessage();
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
}
