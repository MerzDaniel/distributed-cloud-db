package lib.communication;

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

    private final static char GROUP_SEPARATOR = '\u001D';

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

        disconnect();

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
        return readMessage(in);
    }

    /**
     * Sends a message to the connected server
     */
    public void sendMessage(String message) throws IOException {
        sendMessage(out, message);
    }

    /**
     * Reads a message from the connection
     */
    public String readMessage(InputStream in) throws IOException {
        StringBuffer buffer = new StringBuffer();
        while (true) {
            try {
                int val = in.read();
                if (val == -1) {
                    // end of stream
                    throw new IOException("Stream is closed");
                }
                char c = (char) val;
                if (c == '\n') continue;
                if (c == '\r') continue;
                if (c == GROUP_SEPARATOR) {
                    if (buffer.length() > 0)
                        break;
                    else
                        continue;
                }
                buffer.append(c);
            } catch (IOException e) {
                throw e;
            }
        }
        String msg = buffer.toString();

        return msg;
    }

    /**
     * Sends a message to the connected server
     */
    public void sendMessage(OutputStream out, String message) throws IOException {
        try {
            for (char c : message.toCharArray()) {
                out.write((byte) c);
            }
            out.write(GROUP_SEPARATOR);
        } catch (IOException e) {
            throw e;
        }
    }

}
