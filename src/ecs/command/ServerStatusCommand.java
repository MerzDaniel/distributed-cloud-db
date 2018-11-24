package ecs.command;

import ecs.Command;
import ecs.State;
import lib.SocketUtil;
import lib.message.KVAdminMessage;
import lib.message.MessageMarshaller;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

/**
 * This class represent the command for getting the status of a {@link server.KVServer} instance
 */
public class ServerStatusCommand implements Command {
    Logger l = LogManager.getLogger(ServerStatusCommand.class);

    /**
     * Execute the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {
        state.meta.getKvServerList().parallelStream().forEach(sd -> {
            try (Socket s = new Socket(sd.getHost(), sd.getPort());
                 InputStream i = s.getInputStream();
                 OutputStream o = s.getOutputStream()) {
                MessageMarshaller.unmarshall(SocketUtil.readMessage(i));

                SocketUtil.sendMessage(o, MessageMarshaller.marshall(new KVAdminMessage(KVAdminMessage.StatusType.STATUS)));
                KVAdminMessage response = (KVAdminMessage) MessageMarshaller.unmarshall(SocketUtil.readMessage(i));

                System.out.format("Server %s at %s:%d has status: %s\n", sd.getName(), sd.getHost(), sd.getPort(), response.runningState.toString());
            }catch (ConnectException e) {
                System.out.format("Server %s at %s:%d has could not be reached\n", sd.getName(), sd.getHost(), sd.getPort());
            } catch (Exception e) {
                l.warn("Error", e);
                System.out.format("Server %s at %s:%d returned an error\n", sd.getName(), sd.getHost(), sd.getPort());
            }
        });
    }
}
