package ecs.command;

import ecs.Command;
import ecs.State;
import lib.SocketUtil;
import lib.message.KVAdminMessage;
import lib.message.KVMessage;
import lib.message.MessageMarshaller;
import lib.metadata.ServerData;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConfigureAllCommand implements Command {

    @Override
    public void execute(State state) {
        boolean universeIsOk = true;

        for (ServerData sd : state.meta.getKvServerList()) {
            try (Socket s = new Socket(sd.getHost(), sd.getPort())) {
                InputStream i = s.getInputStream();
                KVMessage connectionSuccessMsg = (KVMessage) MessageMarshaller.unmarshall(SocketUtil.readMessage(i));

                OutputStream o = s.getOutputStream();
                KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.CONFIGURE, state.meta);
                SocketUtil.sendMessage(o, MessageMarshaller.marshall(msg));

                KVAdminMessage response = (KVAdminMessage) MessageMarshaller.unmarshall(SocketUtil.readMessage(i));
                if (response.status != KVAdminMessage.StatusType.CONFIGURE_SUCCESS) {
                    System.out.println(String.format("Error while starting server %s:%d : %s", sd.getHost(), sd.getPort(), response.status));
                }
            } catch (Exception e) {
                System.out.println(String.format("Error while starting server %s:%d : %s", sd.getHost(), sd.getPort(), e.getMessage()));
            }
        }

        if (universeIsOk)
            System.out.println("Successfully configured all servers");
        else
            System.out.println("All other servers were configured successfully");
    }
}
