package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        System.out.println("Server started");


        int port;
        if (args.length >0) port = Integer.parseInt(args[0]);
        else port = 50000;

        ServerSocket s;
        try {
            s = new ServerSocket(port);
            while(true) {
                Socket clientSocket = s.accept();
                new Thread(new ConnectionHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
