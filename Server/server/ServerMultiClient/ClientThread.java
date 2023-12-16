package ServerMultiClient;

import FileTransfer.FileTransferServer;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientThread extends Thread {
    protected Socket socket;
    private static Map<String, PrintWriter> connectedClients = new HashMap<>();
    private String username;
    private boolean pongReceived = false;
    private Timer timer;
    private boolean running = true;
    private List<String> gamePlayers;
    private List<String> gameResults;
    private boolean startgame;
    private Scanner scanner;

    public ClientThread(Socket clientSocket,List gamePlayers,List<String> gameResults,boolean startgame) {
        this.socket = clientSocket;
        this.gamePlayers=gamePlayers;
        this.gameResults=gameResults;
        this.startgame=startgame;
    }

    public void run() {
        InputStream input = null;


        try {
            input = socket.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PrintWriter writer = null;
        scanner=new Scanner(input);

        try {
            writer = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (true){
            startPingPongCheck(writer);
        }

        while (socket.isConnected()) {
            try {
                if (socket.isClosed()){
                    return;
                } else {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (scanner.hasNextLine()){
                try {
                    String line = scanner.nextLine();
                    System.out.println(line);
                    if (line.startsWith("IDENT")){
                        UserLogin(writer, line);
                    } else if (line.startsWith("BCST")){
                        UserBroadcast(line, username);
                    } else if (line.startsWith("USERMSG")){
                        UserPrivateMessage(writer, line, username);
                    } else if (line.startsWith("CLIENTS")){
                        ListOfConnectedClients(writer, username);
                    } else if (line.startsWith("QUIT")){
                        UserQuit(writer, username);
                    } else if (line.equals("PONG")){
                        pongReceived = true;
                    } else if (line.startsWith("GAMEREQ")){
                        if (!startgame) {
                            gamePlayers.add(username);
                        guessinggame(username);
                        final PrintWriter finalWriter = writer;
                            // Start a new thread to handle the delay and subsequent processing
                        new Thread(() -> {
                            try {
                                Thread.sleep(10000); // Sleep for 10 seconds
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (gamePlayers.size() < 2) {
                                finalWriter.println("FAIL01: Less than 2 users want to play");
                                finalWriter.flush();
                            } else {
                                int winning=winningNumber();
                                for (String username : gamePlayers){
                                    PrintWriter receiverWriter = connectedClients.get(username);
                                    receiverWriter.println("STARTGAME " + winning);
                                    receiverWriter.flush();

                                }
                                
                            }
                        }).start();
                        }else{
                            writer.println("FAIL02: only 1 game can be running at a time");
                            writer.flush();
                        }
                        
                    }else if(line.startsWith("ADDPLAYER")){
                        gamePlayers.add(line.replaceFirst("ADDPLAYER ", ""));
                        startgame=true;
                    } else if(line.startsWith("GAMEDONE")){
                        gameResults.add(line.replaceFirst("GAMEDONE ",""));
                        if (gameResults.size()==gamePlayers.size()) {
                            writer.println("GAMERES "+gameResults.toString());
                            writer.flush();
                        }
                    }else if (line.startsWith("GETRES")){
                        startgame=false;
                        writer.println("GAMERES "+gameResults.toString());
                        writer.flush();
                    }
                    else if (line.startsWith("FILE")||line.startsWith("TRANSFER")) {
                        //forwards the client req
                        sendMsg(writer,line);

                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }
    // private void startTimer(){
    //     timer = new Timer();
    //     timer.schedule(new TimerTask() {
    //         @Override
    //         public void run() {
    //             timerDone = true;
    //             timer.cancel();  // Stop the timer when 2 minutes have passed
    //         }
    //     }, 1 * 60 * 1000);  // 2 minutes in milliseconds
    //     System.out.println("timer done");
    // }
    public void sendMsg(PrintWriter writer, String line){
        //gets the username of from the command
        String message[]= line.split(" ");
        String receiverUser = message[2];
        if (connectedClients.containsKey(receiverUser)) {
            writer.println("REQTRANSFERSENT [" + receiverUser + "] ");
            writer.flush();
            PrintWriter receiverWriter = connectedClients.get(receiverUser);
            receiverWriter.println(line);
            receiverWriter.flush();
            System.out.println("OK TRANSFERMSG <" + receiverUser + ">");
        } else if (!connectedClients.containsKey(receiverUser)){
            writer.println("FAIL01: User not found");
            writer.flush();
        }
    }
    private void ListOfConnectedClients(PrintWriter writer, String username) {
        // Create a list of usernames
        List<String> usernames = new ArrayList<>(connectedClients.keySet());
        // Send the list of connected clients back to the client
        writer.println("CLIENTS: " + usernames);
        writer.flush();
        System.out.println("OK CLIENTS <" + username + ">");
    }
    private void UserPrivateMessage(PrintWriter writer, String line, String username) {
        //gets the username of from the command
        String message[]= line.split(" ");
        String receiverUser = message[2];
        String messageContent = line.substring(line.indexOf(message[3]));
        if (!connectedClients.containsKey(receiverUser)) {
            writer.println("FAIL01: User not found");
            writer.flush();
        } else {
            writer.println("MSGSENT [" + receiverUser + "] " + messageContent);
            writer.flush();
            PrintWriter receiverWriter = connectedClients.get(receiverUser);
            receiverWriter.println("USERMSG [" + username + "] " + messageContent);
            receiverWriter.flush();
            System.out.println("OK USERMSG <" + receiverUser + ">");
        }
    }
    private void UserBroadcast(String line, String username) {
        //removes the BCST command before broadcasting
        line = line.replaceFirst("BCST ", "");
        //shows up in server terminal
        System.out.println("OK BCST <" + line + ">");
        //shows to all connected users
        for (PrintWriter connectedWriter : connectedClients.values()) {
            connectedWriter.println("BCST " + "[" + username + "]" + " " + line);
            connectedWriter.flush();
        }

    }
    private void guessinggame(String username) {
        for (Map.Entry<String, PrintWriter> entry : connectedClients.entrySet()) {
            String clientUsername = entry.getKey();
            PrintWriter connectedWriter = entry.getValue();
    
            if (!clientUsername.equals(username)) {
                connectedWriter.println("GAMEREQ");
                connectedWriter.flush();
            }
        }
    }
    
    private int winningNumber(){
        Random random =new Random();
        return random.nextInt(50) + 1;
    }
    private void UserLogin(PrintWriter writer, String line) {
        // Removes the command and adds the username to the connectedClients ArrayList
        line = line.replaceFirst("IDENT ", "");
        if (connectedClients.containsKey(line)){
            System.out.println("FAIL01");
            writer.println("FAIL01: User already logged in");
            writer.flush();
        } else if (line.length() < 4){
            System.out.println("FAIL02");
            writer.println("FAIL02: Username has an invalid format or length");
            writer.flush();
        } else {
            writer.println("LOGIN OK");
            writer.flush();
            connectedClients.put(line, writer);
            // Shows all connected users
            System.out.println("OK IDENT " + "<" + line +">");
            username = line;
            // Tells user they are logged in
            // Loop through all connected clients and send the message
            for (PrintWriter connectedWriter : connectedClients.values()){
                connectedWriter.println("JOINED " + "[" + line + "]");
                connectedWriter.flush();
            }
        }
    }
    private void UserQuit(PrintWriter writer, String username) {
        running = false;
        // Remove the client from the connectedClients Map
        connectedClients.remove(username);
        // Notify all connected clients about the disconnect
        for (PrintWriter connectedWriter : connectedClients.values()) {
            try {
                connectedWriter.println("LEFT " + username);
                connectedWriter.flush();
            } catch (Exception e) {
                System.err.println("Error sending message to client: " + e.getMessage());
            }
        }
        // Send a goodbye message to the client
        writer.println("OK Goodbye");
        writer.flush();
        System.out.println("OK GOODBYE " + "<" + username + ">");
        // Close the socket connection
        try {
            writer.close();
        } catch (Exception e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
    private void startPingPongCheck(PrintWriter writer) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!running) {
                    return;
                }
                writer.println("PING");
                writer.flush();
                // Wait for PONG response
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Check if PONG has been received
                if (!pongReceived) {
                // Disconnect client
                    UserQuit(writer, username);
                } else {
                // Reset pongReceived for next iteration
                    pongReceived = false;
                }
            }
        }, 0, 5000);
    }
}
