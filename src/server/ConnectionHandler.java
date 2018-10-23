package server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static lib.SocketUtil.tryClose;

public class ConnectionHandler implements Runnable {
    final Socket s;
    final Logger logger = LogManager.getLogger(ConnectionHandler.class);
    OutputStream o;
    InputStream i;

    public ConnectionHandler(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            i = s.getInputStream();
            o = s.getOutputStream();

            OutputStream o = s.getOutputStream();
            for (char c : "Hello there!".toCharArray()) {
                o.write((byte) c);
            }
            o.write((byte) '\r');
        } catch (IOException e) {
            logger.warn("Error during communication with an open connection:"+ e.getMessage());
            logger.warn(e.getStackTrace());
        } finally {
            tryClose(o);
            tryClose(i);
            tryClose(s);
        }
    }
}

