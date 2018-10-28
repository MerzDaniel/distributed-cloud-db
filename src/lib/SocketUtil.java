package lib;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketUtil {
    final static Logger logger = LogManager.getLogger(SocketUtil.class);

    /**
     * Reads a message from the connection
     */
    public static String readMessage(InputStream in) throws IOException {
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
                throw e;
            }
        }
        String msg = buffer.toString();
        logger.info("Received a message from the server: " + msg);
        return msg;
    }

    /**
     * Sends a message to the connected server
     */
    public static void sendMessage(OutputStream out, String message) throws IOException {
        try {
            for (char c : message.toCharArray()) {
                out.write((byte) c);
            }
            out.write('\r');
        } catch (IOException e) {
            logger.warn("Error in sendMessage(): " + e.getMessage());
            logger.warn(e.getStackTrace());
            throw e;
        }
    }

    /**
     * Checks if the connection is still active.
     */
    public static boolean isConnected(Socket socket) {
        return socket != null
                && socket.isConnected() && !socket.isClosed()
                && !socket.isInputShutdown() && !socket.isOutputShutdown();
    }

    /**
     * Tries to close a closeable.
     * @param closeable
     */
    public static void tryClose(Closeable closeable) {
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            logger.warn("Error on closing "+ closeable.getClass()+ ": " + e.getMessage());
            logger.warn(e.getStackTrace());
        }
    }
}
