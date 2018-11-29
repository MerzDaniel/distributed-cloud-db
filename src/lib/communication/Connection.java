package lib.communication;

import lib.SocketUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static lib.SocketUtil.tryClose;

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
     */
    public void connect(String host, int port) throws IOException {
        logger.debug(String.format("Connect to %s:%d", host, port));

        socket = new Socket(host, port);
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    public void use(Socket s) throws IOException {
        logger.debug(String.format("Use %s:%d", s.getInetAddress(), s.getPort()));

        socket = s;
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    /**
     * Checks if the connection is still active.
     */
    public boolean isConnected() {
        return lib.SocketUtil.isConnected(socket);
    }

    /**
     * Disconnects the current connection
     */
    public void disconnect() {
        if (socket == null) return;
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
    public String readMessage() throws IOException {
        return SocketUtil.readMessage(in);
    }

    /**
     * Sends a message to the connected server
     */
    public void sendMessage(String message) throws IOException {
        SocketUtil.sendMessage(out, message);
    }

}
