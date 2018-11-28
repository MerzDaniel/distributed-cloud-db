package ecs.service;

import lib.SocketUtil;
import lib.communication.Connection;
import lib.message.KVAdminMessage;
import lib.message.KVMessage;
import lib.message.MarshallingException;
import lib.message.MessageMarshaller;
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
    public static KVAdminMessage moveData(ServerData to, Connection con, boolean softMove) throws IOException, MarshallingException {
        KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.MOVE, to);
        con.sendMessage(msg.marshall());
        String responseString = con.readMessage();
        return (KVAdminMessage) MessageMarshaller.unmarshall(responseString);
    }

    public static boolean makeReadonly(ServerData sd, Connection con) throws MarshallingException, IOException {
        KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.MAKE_READONLY);
        con.sendMessage(msg.marshall());
        String responseString = con.readMessage();
        return ((KVAdminMessage) MessageMarshaller.unmarshall(responseString)).status == KVAdminMessage.StatusType.MAKE_SUCCESS;
    }

    public static KVAdminMessage configure(ServerData sd, KVStoreMetaData meta, int index) throws IOException, MarshallingException {
        Socket s = new Socket(sd.getHost(), sd.getPort());
        InputStream i = s.getInputStream();
        KVMessage connectionSuccessMsg = (KVMessage) MessageMarshaller.unmarshall(SocketUtil.readMessage(i));

        if (!connectionSuccessMsg.getStatus().equals(KVMessage.StatusType.CONNECT_SUCCESSFUL)) {
            return new KVAdminMessage(KVAdminMessage.StatusType.CONFIGURE_ERROR);
        }

        OutputStream o = s.getOutputStream();
        KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.CONFIGURE, meta, index);
        SocketUtil.sendMessage(o, MessageMarshaller.marshall(msg));

        KVAdminMessage response = (KVAdminMessage) MessageMarshaller.unmarshall(SocketUtil.readMessage(i));
        return response;
    }
}
