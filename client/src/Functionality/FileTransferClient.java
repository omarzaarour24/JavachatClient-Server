package Functionality;

import Functionality.CheckSum;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class FileTransferClient extends Thread {
    private final String host = "127.0.0.1";
    private final int port = 5001;
    private Socket socket;
    private String type;
    private File file;
    private PrintWriter writer;
    private String filepath;
    //private Scanner scanner;
    private String username;
    private String otheruser;
    private InputStream inputStream;
    private CheckSum checkSum;
    public FileTransferClient(String type, String filepath, String username, String otheruser,CheckSum checkSum) {
        this.type = type;
        this.filepath = filepath;
        this.username = username;
        this.otheruser = otheruser;
        this.checkSum=checkSum;
    }


    @Override
    public void run() {
        try {
            // Connect to the server
            socket = new Socket(host, port);
            System.out.println("Connected to file Transfer Server!! ");

            // Send the appropriate message to the server to identify this client as a sender or receiver
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output);
            writer.println("NEWUSER " + username + " " + otheruser + " " + type);
            writer.flush();

            inputStream = socket.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            String input = scanner.nextLine();
            if (input != null) {
                // Process the input
                if (input.equals("TRANSFER START")) {
                    System.out.println("starting transfer");
                    if (type.equals("s")) {
                        file = new File(filepath);
                        System.out.println("sender");
                        //sendFile();
                        //send(file,output);

                        SendFile();
                    } else if (type.equals("r")) {
                        file = new File(filepath);
                        System.out.println("reciever");
                        ReceiveFile();
                        verifyDownload();

                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void ReceiveFile(){
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    // Write incoming data to file
                    writer.write(line);
                    writer.newLine();
                }
                System.out.println("Incoming data saved to " + filepath);

            } catch (IOException e) {
                e.printStackTrace();
            }


    }
    public void SendFile() throws IOException {

        System.out.println("Starting file upload...");

        // Get the output stream for sending the file data
        OutputStream outputStream = socket.getOutputStream();

        // Open the file and get the input stream for reading the file data
        FileInputStream fileInputStream = new FileInputStream(filepath);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        // Create a byte buffer to hold the file data
        byte[] buffer = new byte[8192];
        int bytesRead = 0;

        // Read the file data into the buffer and write it to the output stream
        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        // Flush and close the output stream and input stream
        outputStream.flush();
        outputStream.close();
        bufferedInputStream.close();
        fileInputStream.close();

        System.out.println("File upload completed.");

//        // Open a FileInputStream for the file
//        FileInputStream fileInputStream = new FileInputStream(file);
//
//        // Transfer the file data to the server
//        System.out.println("UPLOADING FILE");
//        OutputStream outputStream = socket.getOutputStream();
//        fileInputStream.transferTo(outputStream);
//
//        // Close the FileInputStream
//        fileInputStream.close();
//        System.out.println("UPLOAD COMPLETE ");
//        socket.close();
//        System.out.println("Disconnected from file Transfer Server!!");
    }
    public void verifyDownload() throws Exception {
        //checksum
        if (checkSum.compare(checkSum.create(file))){
            System.out.println("file integrity intact, verified ");
        }else if (!checkSum.compare(checkSum.create(new File(filepath)))){
            file.delete();
            System.out.println("file corrupted during transfer, file has been deleted please try again ");
        }
    }
}