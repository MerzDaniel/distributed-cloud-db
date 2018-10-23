package lib;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class SocketUtil {
    final static Logger logger = LogManager.getLogger(SocketUtil.class);

    /**
     * Checks if the connection is still active.
     */
    public static boolean isConnected(Socket socket) {
        return socket != null && socket.isConnected() && !socket.isClosed();
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
