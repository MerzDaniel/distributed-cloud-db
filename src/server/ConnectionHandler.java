package server;

import lib.SocketUtil;
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

    public ConnectionHandler(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        OutputStream o = null;
        InputStream i = null;

        try {
            i = s.getInputStream();
            o = s.getOutputStream();

            SocketUtil.sendMessage(o, "Successfully connected.");

            while (SocketUtil.isConnected(s)) {
                String msg = SocketUtil.readMessage(i);
                SocketUtil.sendMessage(o, "ECHO: " + msg);
            }
        } catch (IOException e) {
            logger.warn("Error during communication with an open connection:" + e.getMessage());
            logger.warn(e.getStackTrace());
        } finally {
            tryClose(o);
            tryClose(i);
            tryClose(s);
        }
    }
}

