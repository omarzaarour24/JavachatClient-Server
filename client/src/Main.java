 import Functionality.Dialog;

import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[]args) throws IOException {

        Socket socket = new Socket("127.0.0.1", 5000);
        Dialog dialog=new Dialog(socket);
        InputThread inputThread = new InputThread(socket,dialog);
        inputThread.start();
        OutputThread outputThread = new OutputThread(socket, dialog);
        outputThread.start();
    }
}
