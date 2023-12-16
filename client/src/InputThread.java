import Functionality.Dialog;

import java.io.*;
import java.net.Socket;


public class InputThread extends Thread{
    private Socket socket;
    private boolean IsLogedin=false;
    private PrintWriter writer;
    private Dialog dialog;



    public InputThread(Socket socket,Dialog dialog) throws IOException {
        this.socket = socket;
        this.dialog=dialog;
        OutputStream outputStream = socket.getOutputStream();
        writer = new PrintWriter(outputStream);
    }
    @Override
    public void run() {
        dialog.header();
        try {
            dialog.isLogged();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        dialog.menu();
        while (socket.isConnected()){
            try {
                dialog.input();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}

