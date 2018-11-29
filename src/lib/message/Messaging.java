package lib.message;

import lib.communication.Connection;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;

public class Messaging {
    private static Logger logger = LogManager.getLogger(Messaging.class);
    private Connection con;

    Iterator<IMessage> messageIterator;

    public Messaging() {
    }

    public boolean connect(ServerData sd) throws IOException {
        return connect(sd.getHost(), sd.getPort());
    }

    public boolean connect(String host, int port) throws IOException {
        Connection c = new Connection();
        c.connect(host, port);
        connect(c);
        return ((KVMessage)readMessage()).getStatus() == KVMessage.StatusType.CONNECT_SUCCESSFUL;
    }

    public boolean connect(Socket s) throws IOException {
        Connection c = new Connection();
        c.use(s);
        connect(c);
        return true;
    }

    private void connect(Connection con) {
        this.con = con;
        messageIterator = new Iterator<IMessage>() {
            IMessage nextMsg;

            @Override
            public boolean hasNext() {
                if (nextMsg != null) return true;
                nextMsg = readNextMessage(con);
                return nextMsg != null;
            }

            @Override
            public IMessage next() {
                IMessage result = nextMsg;
                nextMsg = null;
                return result;
            }
        };
    }

    public IMessage readMessage() throws IOException {
        if (!con.isConnected() || !messageIterator.hasNext()) throw new IOException();

        return messageIterator.next();
    }

    public void sendMessage(IMessage msg) throws MarshallingException, IOException {
        con.sendMessage(msg.marshall());
    }

    public boolean isConnected() {
        return con.isConnected();
    }

    public void disconnect() {
        con.disconnect();
        messageIterator = null;
    }

    private static IMessage readNextMessage(Connection con) {
        while (con.isConnected()) {
            try {
                String msg = con.readMessage();
                if (msg.length() == 0) {
                    logger.debug(String.format("Empty message"));
                    continue;
                }
                return MessageMarshaller.unmarshall(msg);
            } catch (IOException e) {
                logger.warn("IO Exception will close Messaging!", e);
                break;
            } catch (MarshallingException e) {
                logger.warn("Marsharhalling exception!", e);
                continue;
            }
        }
        return null;
    }
}
