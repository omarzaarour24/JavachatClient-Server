package FileTransfer;


import FileTransfer.Clients;
import FileTransfer.TransferHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class FileTransferServer extends Thread{
    private Clients clients=new Clients();
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(5001);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Transfer server started, listening on port 5001");

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Transfer Client connected: " + clientSocket.getInetAddress().getHostAddress());
                Thread transferThread = new Thread(new TransferHandler(clientSocket,clients));
                transferThread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }
}
