package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    final Socket s;

    public ConnectionHandler(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            OutputStream o = s.getOutputStream();
            for (char c : "Hello there!".toCharArray()) {
                o.write((byte) c);
            }
            o.write((byte) '\r');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

