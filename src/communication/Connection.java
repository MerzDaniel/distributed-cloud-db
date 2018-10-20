package communication;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

public class Connection {
    Logger logger = LogManager.getLogger(Connection.class);

    Socket socket;

    public boolean connect(String host, int port) {
        logger.info(String.format("Connect to %s:%d", host, port));
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            logger.warn(String.format("Connecting to %s:%d failed: %s", host, port, e.getMessage()));
            logger.warn(e.getStackTrace());
            return false;
        }
        return true;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
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

}
