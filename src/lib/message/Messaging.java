package lib.message;

import lib.communication.Connection;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;

public class Messaging {
    private static Logger logger = LogManager.getLogger(Messaging.class);
    private Connection con;

    Iterator<IMessage> messageIterator;

    public Messaging() {
    }

    public void connect(String host, int port) throws IOException {
        con.connect(host, port);
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
                return nextMsg;
            }
        };
    }
    public IMessage readMessage() {
        return messageIterator.next();
    }

    private static IMessage readNextMessage(Connection con) {
        while(con.isConnected()) {
            try {
                String msg = con.readMessage();
                if (msg.length() == 0) continue;
                return MessageMarshaller.unmarshall(msg);
            } catch (IOException e) {
                logger.warn(e);
                break;
            } catch (MarshallingException e) {
                logger.warn(e);
                continue;
            }
        }
        return null;
    }
}
