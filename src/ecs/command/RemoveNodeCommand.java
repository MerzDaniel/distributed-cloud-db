package ecs.command;

import ecs.Command;
import ecs.State;
import lib.communication.Connection;
import lib.message.KVAdminMessage;
import lib.message.MarshallingException;
import lib.message.MessageMarshaller;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class RemoveNodeCommand implements Command {
    private String name;
    private Logger logger = LogManager.getLogger(RemoveNodeCommand.class);

    public RemoveNodeCommand(String name) {
        this.name = name;
    }

    @Override
    public void execute(State state) {
        if (state.meta.getKvServerList().size() <= 0) {
            System.out.println("Cannot remove a node when less than 2 are running");
            return;
        }
        ServerData from = null, to = null;
        List<ServerData> list = state.meta.getKvServerList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(name)) {
                from = list.get(i);
                to = list.get(i + 1 % list.size());
                break;
            }
        }
        if (from == null || to == null) {
            System.out.println("Server does not exist and can therefor not be removed.");
            return;
        }

        KVAdminMessage msg = new KVAdminMessage(KVAdminMessage.StatusType.MOVE, to);
        Connection con = new Connection();
        try {
            con.connect(from.getHost(), from.getPort());
            con.readMessage();
        } catch (IOException e) {
            logger.warn("Error.", e);
            System.out.println("Error while connecting to the server.");
            return;
        }

        try {
            con.sendMessage(msg.marshall());
            String responseString = con.readMessage();
            KVAdminMessage response = (KVAdminMessage) MessageMarshaller.unmarshall(responseString);
            if (response.status != KVAdminMessage.StatusType.MOVE_SUCCESS)
                System.out.println("Data was not successfully moved. But I'm evil and still shutting down the node ^_^");

            KVAdminMessage stopMsg = new KVAdminMessage(KVAdminMessage.StatusType.SHUT_DOWN);
            con.sendMessage(stopMsg.marshall());
            responseString = con.readMessage();
            response = (KVAdminMessage) MessageMarshaller.unmarshall(responseString);
            if (response.status != KVAdminMessage.StatusType.SHUT_DOWN_SUCCESS)
                System.out.println("Was not successfully shutdown");
            else
                System.out.println("Successfully shutdown the node");

        } catch (Exception e) {
            logger.warn("Error.", e);
            System.out.println("Error during removing the server and moving the data");
            return;
        }
    }
}