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

    public synchronized boolean connect(ServerData sd) throws IOException {
        return connect(sd.getHost(), sd.getPort());
    }

    public synchronized boolean connect(String host, int port) throws IOException {
        Connection c = new Connection();
        c.connect(host, port);
        connect(c);
        return ((KVMessage)readMessage()).getStatus() == KVMessage.StatusType.CONNECT_SUCCESSFUL;
    }

    public synchronized boolean connect(Socket s) throws IOException {
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

    public synchronized IMessage readMessage() throws IOException {
        if (!con.isConnected() || !messageIterator.hasNext()) throw new IOException();

        return messageIterator.next();
    }

    public synchronized void sendMessage(IMessage msg) throws MarshallingException, IOException {
        con.sendMessage(msg.marshall());
    }

    public boolean isConnected() {
        return con.isConnected();
    }

    public void disconnect() {
        con.disconnect();
    }

    private IMessage readNextMessage(Connection con) {
        while (con.isConnected()) {
            try {
                String msg = con.readMessage();
                logger.debug(String.format("Got a message: %s\n", msg));
                if (msg.length() == 0) {
                    continue;
                }
                return MessageMarshaller.unmarshall(msg);
            } catch (IOException e) {
                logger.debug("Connection seems to be closed", e);
                disconnect();
                break;
            } catch (MarshallingException e) {
                logger.warn("Marsharhalling exception!", e);
                continue;
            }
        }
        return null;
    }
}
