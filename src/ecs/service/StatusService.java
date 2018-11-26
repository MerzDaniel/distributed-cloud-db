package ecs.service;

import lib.SocketUtil;
import lib.message.KVAdminMessage;
import lib.message.MarshallingException;
import lib.message.MessageMarshaller;
import lib.metadata.ServerData;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class StatusService {
    private static Logger l = LogManager.getLogger(StatusService.class);

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
}