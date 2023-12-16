package Functionality;
import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import static java.lang.Thread.sleep;
public class Dialog {
    private String username;
    private PrintWriter writer;
    private Scanner ReadInput;
    private String filepath;
    private FileTransferClient fileTransferClient;
    private boolean isLogged;
    private Socket socket;
    private String reply;
    private boolean transferReq;
    private String OtherUser;
    private String filename;
    private boolean gamereq=false;
    private boolean stopGame = false;

   private CheckSum checkSum=new CheckSum();


    public Dialog(Socket socket) throws IOException {
        this.socket = socket;
        OutputStream outputStream = socket.getOutputStream();
        writer = new PrintWriter(outputStream);
        ReadInput = new Scanner(System.in);

    }

    public void input() throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (!gamereq) {
            String line = scanner.nextLine();
            switch (line) {
                case "1" -> broadcast();
                case "2" -> msgUser();
                case "3" -> {
                    System.out.println("A request has been sent, please wait for players to join");
                    writer.println("GAMEREQ " + username);
                    writer.flush();
                    gamereq=true;
                }
                case "4" -> {
                    writer.println("DSCN");
                    writer.flush();
                }
                //System.exit(1);
                case "5" -> {
                    writer.println("CLIENTS");
                    writer.flush();
                }
                case "6" -> {
                    writer.println("QUIT");
                    writer.flush();
                    System.exit(1);
                }
                case "7" -> transferStart();
                case "help" -> System.out.println("help works");//dialog.HelpMenu();
                case  "Y" -> {
                    if (gamereq){
                        writer.println("ADDPLAYER "+username);
                        writer.flush();
//                        gamereq=false;
                    }
                    else if (transferReq){
                        System.out.println("Accepted file transfer");
                        accept();
                    }else if (!transferReq){
                        System.out.println("INVALID OPTION");
                    }
                }
                case "n"->{
                    if (transferReq){
                        System.out.println("Rejected file transfer");
                        reject();
                    }else if (!transferReq){
                        System.out.println("INVALID OPTION");
                    }
                }
                default -> System.out.println("INVALID OPTION");
            }
        }
    }
    public void output(String line) throws IOException {
        if(line != null){
            reply=line;
            if (line.equals("PING")){
                keeprunning();
            }
            if (line.equals("LOGIN")){
                isLogged=true;
            }
            if (line.startsWith("BCST")){
                System.out.println(line);
            }
            if (line.startsWith("GAMEREQ")){
                gamereq=true;
                System.out.println("A guessing game is about to start would u like to play? (Y/n)");

            }
            if (line.startsWith("CLIENTS")){
                System.out.println(line);
            }
            if (line.startsWith("STARTGAME")){
                new Thread(() -> {
                    playgame(Integer.parseInt(line.replaceAll("STARTGAME ", "")));
                }).start();                
            }
            if (line.startsWith("JOINED")){
                System.out.println(line);
            }
            if (line.startsWith("FAIL")){
                if(line.equals("FAIL01: Less than 2 users want to play")){
                    gamereq=false;
                }
                System.out.println(line);
            }
            if (line.startsWith("MSGSENT")){
                System.out.println(line);
            }
            if (line.startsWith("USERMSG")){
                System.out.println(line);
            }
            if (line.startsWith("LEFT")){
                System.out.println(line);
            }
            if (line.startsWith("OK")){
                System.out.println(line);
            }
            if (line.startsWith("FILE")){
                transferReq=true;
                filereq(line);

            }
            if (line.startsWith("TRANSFER")){
                System.out.println("transfer msg received");
                //time.resetTime();
                if (line.contains("ACCEPT")){
                    System.out.println("Your transfer request has been accepted, upload will start now !! ");
                    fileTransferClient=new FileTransferClient("s",filepath, username, OtherUser,checkSum);
                    Thread thread=new Thread(fileTransferClient);
                    thread.start();


                } else if (line.contains("REJECT")) {
                    System.out.println("the transfer file was rejected :(");
                }
            }
            if (line.contains("FAIL02 Username has an invalid format or length")){
                System.out.println("Username invalid please try another");

            }
            if (line.startsWith("GAMERES")){
                stopGame=true;
                System.out.println("Lets see the results!");
                displayResults(line);

            }
        }

    }
    
    
    private static void displayResults(String resultsString) {
        // Remove square brackets if present
        resultsString = resultsString.replaceAll("\\[|\\]", "");
    
        // Remove the "GAMERES " prefix
        String[] resultsArray = resultsString.replace("GAMERES ", "").split(", ");
    
        if (resultsArray.length == 1 && resultsArray[0].equalsIgnoreCase("No players guessed in time :(")) {
            System.out.println(resultsArray[0]);
            return;
        }
    
        // Iterate through the array and format each result
        for (int i = 0; i < resultsArray.length; i++) {
            // Split the result into username and score
            String[] parts = resultsArray[i].split("\\(");
    
            if (parts.length == 2) {
                String username = parts[0].trim();
                String score = parts[1].replace(")", "").trim();
    
                // Display the formatted result
                if (i == 0) {
                    System.out.println(i+1+"- "+username + " (winner, " + score + ")");
                } else {
                    System.out.println(i+1+"- "+username + " (" + score + ")");
                }
            }
        }
    }
    
    
    public void playgame(int winningNumber) {
        boolean win = false;
        long startTime = System.currentTimeMillis();    

        while (!win&&!stopGame) {
                if ((System.currentTimeMillis() - startTime)>=1 * 60 * 1000) {
                    System.out.println("sorrry u did not guess in time :(");
                    writer.println("GETRES");
                    writer.flush();
                    break;
                }
                System.out.print("Guess the number:");
                int answer = ReadInput.nextInt();
                if (answer == winningNumber) {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    System.out.println("Correct! It took " + duration + " milliseconds.");
                    System.out.println("Please wait for the results");
                    writer.println("GAMEDONE "+username+" ("+duration+")");
                    writer.flush();
                    win = true;
                } else {
                    if (answer>winningNumber){
                    System.out.println("lower");
                    }else{
                    System.out.println("higher");
                    }
                }
        }
        if (win) {
            try {
                Thread.sleep(1 * 60 * 1000 - (System.currentTimeMillis() - startTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        writer.println("GETRES");
        writer.flush();
    }

    
    public void isLogged() throws InterruptedException {
        while(!isLogged){
            login();
            sleep(100);
            if (reply.startsWith("FAIL")){
                isLogged=false;
                System.out.println(reply);
            } else {
                isLogged=true;
            }
        }

    }
    
    public void header(){
        System.out.println("");
        System.out.println("");
        System.out.println("   Welcome to our Chat client!!   ");
        System.out.println("---------------------------------------------------");
    }
    
    public void menu(){
        System.out.println("Welcome "+username);
        System.out.println("-----------------------------");
        System.out.println("1- Broadcast");
        System.out.println("2- Chat with another user");
        System.out.println("3- Start multiplayer guessing game");
        System.out.println("4- Disconnect");
        System.out.println("5- View list of connected clients");
        System.out.println("6-QUit");
        System.out.println("7-Send file");
        //System.out.println("Type help for more");
        System.out.println("---------------------------------------------------");
        System.out.print("Please select an option: ");
    }

    public void transferStart() throws Exception {
        //enter other user's username
        System.out.print("Please enter the name of the other user:");
        OtherUser = ReadInput.nextLine();
        System.out.print("Please enter the path of the file, Example(client\\src\\FileTransferinput\\test.txt):");
        filepath = ReadInput.nextLine();

        //makes sure the filepath is entered correctly
        // while (!filepath.contains("/")){
        //     System.out.print("Invalid file path syntax, please try again: ");
        //     filepath = ReadInput.nextLine();
        // }
        // filepath="C:\\Users\\omar\\Desktop\\41\\client\\src\\FileTransferinput\\test.txt";
        Path path = Paths.get(filepath);
        filename = path.getFileName().toString();
        File newfile = path.toFile();
        String sum= checkSum.create(newfile);
        //System.out.println("checksum "+sum);
        writer.println("FILE "+username+" "+OtherUser+" "+filename+" "+sum);
        writer.flush();
        System.out.println("File transfer request has been, you will be notified once a reply is received");
    }
    
    public void filereq(String text) {
        String[] msg = text.split(" ");
        OtherUser = msg[1];
        filename=msg[3];
        checkSum.setResult(msg[4]);
        System.out.print("Incoming file from " + OtherUser + ", would you like to accept it? (Y/n)");

    }
    
    public void accept(){
        System.out.println("You have accepted the file transfer, downloading will start now");
        writer.println("TRANSFER ACCEPT " + OtherUser);
        writer.flush();
        System.out.println("here");
        String path="client\\src\\FileTransferOutput\\"+filename;
        fileTransferClient=new FileTransferClient("r",path,username, OtherUser,checkSum);
        Thread filetransferThread=new Thread(fileTransferClient);
        filetransferThread.start();


    }
    
    public void reject(){
        System.out.println("You have rejected the file transfer");
        writer.println("TRANSFER REJECT");
        writer.flush();
    }
    
    public void login(){
        System.out.print("Please enter your username: ");
        username = ReadInput.nextLine();
        while (username.length()<4){
            System.out.print("username must be at least 4 characters long, try again:");
            username = ReadInput.nextLine();
        }
        writer.println("IDENT "+ username);
        writer.flush();

    }

    public String getUsername() {
        return username;
    }

    //sends pong
    public void keeprunning(){
        writer.println("PONG");
        writer.flush();
    }
    public void broadcast(){
        System.out.print("Please enter the breadcast msg: ");
        String breadcastmsg = ReadInput.nextLine();
        writer.println("BCST " + breadcastmsg);
        writer.flush();
    }
    //option 2
    public void msgUser(){
        System.out.print("Please enter the username of the other user: ");
        String OtherUserName = ReadInput.nextLine();
        System.out.print("Please enter the msg you want to send to the other user: ");
        String MsgToUser = ReadInput.nextLine();
        //sends msg to the other user
        writer.println("USERMSG " + username + " " + OtherUserName+" "+MsgToUser);
        writer.flush();
    }

    //option 5
    public void viewConnectedClients(){
        // send a request to the server to get the list of connected clients
        writer.println("GET_CONNECTED_CLIENTS");
        writer.flush();
    }
}
