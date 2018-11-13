package ecs.command;

import ecs.State;
import lib.SocketUtil;
import lib.message.KVAdminMessage;
import lib.message.KVMessage;
import lib.message.MessageMarshaller;
import lib.metadata.ServerData;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class StartServersCommand implements ecs.Command {
    @Override
    public void execute(State state) {
        for (ServerData sd : state.meta.getKvServerList()) {
            try (Socket s = new Socket(sd.getHost(), sd.getPort())) {
                InputStream i = s.getInputStream();
                KVMessage connectionSuccessMsg = (KVMessage) MessageMarshaller.unmarshall(SocketUtil.readMessage(i));

                OutputStream o = s.getOutputStream();
                KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.START);
                SocketUtil.sendMessage(o, MessageMarshaller.marshall(msg));

                KVAdminMessage response = (KVAdminMessage) MessageMarshaller.unmarshall(SocketUtil.readMessage(i));
                if (response.status != KVAdminMessage.StatusType.START_SUCCESS) {
                    System.out.println(String.format("Error while starting server %s:%d : %s", sd.getHost(), sd.getPort(), response.status));
                }

            } catch (Exception e) {
                System.out.println(String.format("Error while starting server %s:%d : %s", sd.getHost(), sd.getPort(), e.getMessage()));
            }
        }
    }
}
