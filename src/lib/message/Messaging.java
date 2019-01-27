package lib.message;

import lib.communication.Connection;
import lib.message.exception.MarshallingException;
import lib.message.kv.KVMessage;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

public class Messaging implements AutoCloseable{
    public static final int CONNECT_RETRIES = 3;
    public static long READ_MESSAGE_TIMEOUT = 7000;

    private static Logger logger = LogManager.getLogger(Messaging.class);
    private Connection con;
    private String host;
    private int port;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Messaging() {
    }

    public Messaging(ServerData sd) throws IOException {
        connect(sd);
    }

    public Messaging(String server, int port) throws IOException {
        connect(server, port);
    }

    public synchronized boolean connect(ServerData sd) throws IOException {
        return connect(sd.getHost(), sd.getPort());
    }

    public synchronized boolean connect(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        int retryCounter = 0;
        IOException lastException = null;
        while (retryCounter++ < CONNECT_RETRIES) {
            try {
                this.con = new Connection();
                this.con.connect(host, port);
                KVMessage kvMessage = (KVMessage) readMessage();
                return kvMessage.getStatus() == KVMessage.StatusType.CONNECT_SUCCESSFUL;
            } catch (IOException e) {
                lastException = e;
            }
        }
        throw new IOException(String.format("Failed to connect to %s:%d after %d retries", host, port, CONNECT_RETRIES), lastException);
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
        return readMessageWithSpecificTimeout(READ_MESSAGE_TIMEOUT);
    }

    public synchronized IMessage readMessageWithoutTimeout() throws IOException {
        return readMessageWithSpecificTimeout(0);
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

    private IMessage readMessageWithSpecificTimeout(long timeout) throws IOException {
        Future<IMessage> messageFuture;
        try {
            messageFuture = readNextMessage();
            if (timeout <= 0)
                return messageFuture.get();
            else
                return messageFuture.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof IOException)) throw new IOException(e.getCause());

            boolean isConnected = isConnected();
            if (!isConnected) {
                // reconnect and try reading again
                connect(host, port);
            }

            messageFuture = readNextMessage();
            try {
                return messageFuture.get(READ_MESSAGE_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (ExecutionException executionException) {
                throw new IOException("Exception after second try. Before Messaging was connected: " + isConnected, executionException.getCause());
            } catch (Exception e1) {
                throw new IOException(e1);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private Future<IMessage> readNextMessage() {
        return executorService.submit(() -> {
            while (con.isConnected()) {
                try {
                    String msg = con.readMessage();
                    if (msg.length() == 0) {
                        continue;
                    }
                    return MessageMarshaller.unmarshall(msg);
                } catch (MarshallingException e) {
                    logger.warn("Marshalling exception!", e);
                    continue;
                }
            }
            throw new IOException("Not connected");
        });
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }
}
