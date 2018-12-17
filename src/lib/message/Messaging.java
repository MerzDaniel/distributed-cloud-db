package lib.message;

import lib.communication.Connection;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

public class Messaging {
    public static final int CONNECT_RETRIES = 10;
    public static long READ_MESSAGE_TIMEOUT = 15000;

    private static Logger logger = LogManager.getLogger(Messaging.class);
    private Connection con;
    private String host;
    private int port;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Messaging() {
    }

    public synchronized boolean connect(ServerData sd) throws IOException {
        return connect(sd.getHost(), sd.getPort());
    }

    public synchronized boolean connect(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        int retryCounter = 0;
        while (retryCounter++ < CONNECT_RETRIES) {
            try {
                this.con = new Connection();
                this.con.connect(host, port);
                KVMessage kvMessage = (KVMessage) readMessage();
                return kvMessage.getStatus() == KVMessage.StatusType.CONNECT_SUCCESSFUL;
            } catch (IOException e) {
            }
        }
        throw new IOException("Failed to connect after " + CONNECT_RETRIES + " retries");
    }

    public synchronized boolean connect(Socket s) throws IOException {
        disconnect();
        host = s.getInetAddress().getHostAddress();
        port = s.getPort();

        con = new Connection();
        con.use(s);
        return true;
    }

    public synchronized IMessage readMessage() throws IOException {
        Future<IMessage> messageFuture;
        try {
            messageFuture = readNextMessage();
            return messageFuture.get(READ_MESSAGE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            if (isConnected()) throw new IOException(e);

            // reconnect and try reading again
            connect(host, port);
            messageFuture = readNextMessage();
            try {
                return messageFuture.get(READ_MESSAGE_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (Exception e1) {
                throw new IOException(e1);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public synchronized void sendMessage(IMessage msg) throws MarshallingException, IOException {
        con.sendMessage(msg.marshall());
    }

    public boolean isConnected() {
        return con.isConnected();
    }

    public void disconnect() {
        if (con == null) return;
        con.disconnect();
    }

    private Future<IMessage> readNextMessage() {
        return executorService.submit(() -> {
            while (con.isConnected()) {
                try {
                    String msg = con.readMessage();
                    logger.debug(String.format("Got a message: %s\n", msg));
                    if (msg.length() == 0) {
                        continue;
                    }
                    return MessageMarshaller.unmarshall(msg);
                } catch (IOException e) {
                    if (con.isConnected()) throw e; // some other problem occurred

                    logger.debug("Connection seems to be closed", e);
                    // Everything is going down!!! Abort mission, I SAID ABORT MISSION!!!!!
                    disconnect();
                    break;
                } catch (MarshallingException e) {
                    logger.warn("Marshalling exception!", e);
                    continue;
                }
            }
            throw new IOException("Not connected");
        });
    }
}
