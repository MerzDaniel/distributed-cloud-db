package lib;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Provides methods for reading writing messages and gracefully handling of sockets
 */
public class SocketUtil {
    final static Logger logger = LogManager.getLogger(SocketUtil.class);
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
