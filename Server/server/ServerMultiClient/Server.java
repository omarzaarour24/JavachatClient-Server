package ServerMultiClient;

import FileTransfer.FileTransferServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class Server {

    static final int PORT = 5000;

    public static void main(String args[]) throws IOException {
        boolean startGame=false;
        List<String> gamePlayers= new ArrayList<>();
        List<String> gameResults=new ArrayList<>();
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started, listening on port " + PORT);

        // Start the file transfer server
        FileTransferServer fileTransferServer = new FileTransferServer();
        fileTransferServer.start();
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
            // new thread for a client
            new ClientThread(socket,gamePlayers,gameResults,startGame).start();
        }
    }

    
}
