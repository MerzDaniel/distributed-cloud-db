package client.communication;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Manages a TCP connection. All functions are save to use without caring about Exceptions
 */
public class Connection {
    Logger logger = LogManager.getLogger(Connection.class);
    Socket socket;
    InputStream in;
    OutputStream out;

    /**
     * Connects to a host and port.
     * @return False if connection could not be established. True Otherwise
     */
    public boolean connect(String host, int port) {
        logger.info(String.format("Connect to %s:%d", host, port));

        try {
            socket = new Socket(host, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            logger.warn(String.format("Connecting to %s:%d failed: %s", host, port, e.getMessage()));
            logger.warn(e.getStackTrace());
            return false;
        }
        return true;
    }

    /**
     * Checks if the connection is still active.
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Disconnects the current connection
     */
    public void disconnect() {
        logger.info(
                String.format(
                        "Closing connection from %s:%d",
                        socket.getInetAddress(), socket.getPort()
                )
        );
        tryClose(in);
        tryClose(out);
        tryClose(socket);
        in = null;
        out = null;
        socket = null;
    }

    /**
     * Reads a message from the connection
     */
    public String readMessage() {
        if (!isConnected()) return "";
        StringBuffer buffer = new StringBuffer();
        while (true) {
            try {
                int val = in.read();
                if (val == -1) {
                    logger.info("readLine(): end of stream reached");
                    break;
                }
                char c = (char) val;
                if (c == '\n') continue;
                if (c == '\r') break;
                buffer.append(c);
            } catch (IOException e) {
                logger.warn("Exception occured while reading message: " + e.getMessage());
                logger.warn(e.getStackTrace());
                break;
            }
        }
        String msg = buffer.toString();
        logger.info("Received a message from the server: " + msg);
        return msg;
    }

    /**
     * Sends a message to the connected server
     */
    public void sendMessage(String message) {
        if (!isConnected()) return;

        try {
            for (char c : message.toCharArray()) {
                out.write((byte) c);
            }
            out.write('\r');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to close a closeable.
     * @param closeable
     */
    private void tryClose(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            logger.warn("Error on closing "+ closeable.getClass()+ ": " + e.getMessage());
            logger.warn(e.getStackTrace());
        }
    }
}
