package communication;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection {
    Logger logger = LogManager.getLogger(Connection.class);
    Socket socket;
    InputStream in;
    OutputStream out;

    public String connect(String host, int port) {
        logger.info(String.format("Connect to %s:%d", host, port));
        String message = "";
        try {
            socket = new Socket(host, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();

            message = readMessage();

        } catch (IOException e) {
            logger.warn(String.format("Connecting to %s:%d failed: %s", host, port, e.getMessage()));
            logger.warn(e.getStackTrace());
            return "";
        }
        return message;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void disconnect() {
        if (!isConnected()) return;
        try {
            socket.close();
        } catch (IOException e) {
            logger.warn(
                    String.format(
                            "Closing connection failed for %s:%d failed: %s",
                            socket.getInetAddress(), socket.getPort(), e.getMessage()
                    )
            );
            logger.warn(e.getStackTrace());
        }
    }

    /**
     * Reads a message from the connection
     * <p>
     * Handles all errors gracefully and returns empty string on errors.
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
}
