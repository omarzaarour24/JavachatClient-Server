package FileTransfer;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class TransferHandler implements Runnable {
    private final Socket socket;
    private boolean isSender;

    private Clients clients;
    //private PrintWriter writer;
    private Scanner scanner;
    private Client client;
    private String ReceiverUsername;
    private String SenderUsername;
    private String type;
    public TransferHandler(Socket socket, Clients clients) {
        this.socket = socket;
        this.clients = clients;

    }

    public void run() {
        InputStream input = null;


        try {
            input = socket.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        scanner=new Scanner(input);

        String line = scanner.nextLine();

            System.out.println("input: "+line);

            if (line.startsWith("NEWUSER")){
                String message[]= line.split(" ");
                String username=message[1];
                String otheruser=message[2];
                type=message[3];
                try {
                    PrintWriter writer = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
                    client=new Client(username,otheruser,writer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (type.equals("r")){
                    clients.setReceiver(socket, client);
                    ReceiverUsername=message[1];
                    SenderUsername=message[2];
                    System.out.println("Client is a receiver.");
                }else if(type.equals("s")){
                    clients.setSender(socket, client);
                    SenderUsername=message[1];
                    ReceiverUsername=message[2];
                    System.out.println("Client is a sender.");
                }

            }

            if (clients.isBotHere()) {
                try {
                    PrintWriter writer = new PrintWriter(new DataOutputStream(clients.getSender(SenderUsername).getOutputStream()));
                    writer.println("TRANSFER START");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    PrintWriter writer = new PrintWriter(new DataOutputStream(clients.getReceiver(ReceiverUsername).getOutputStream()));
                    writer.println("TRANSFER START");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Send the file data to the receiver
                // Get the output stream for the other socket
                if (type.equals("s")){
                    try {
                        input.transferTo(clients.getReceiver(ReceiverUsername).getOutputStream());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else if (type.equals("r")){
                    InputStream inn= null;
                    try {
                        inn = clients.getSender(SenderUsername).getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        inn.transferTo(socket.getOutputStream());inn.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    OutputStream outputStream = clients.getReceiver(ReceiverUsername).getOutputStream();
                    DataOutputStream stream =new DataOutputStream(clients.getReceiver(ReceiverUsername).getOutputStream());
                    System.out.println("got filedata");
                    sleep(1000);



                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

