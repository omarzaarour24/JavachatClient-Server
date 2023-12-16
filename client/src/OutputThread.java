import Functionality.Dialog;
import Functionality.ServerCheck;

import java.io.*;
import java.net.Socket;

public class OutputThread extends Thread{
    private Socket socket;
    private BufferedReader reader;
    private String line;
    private ServerCheck time = new ServerCheck();
    private Dialog dialog;

    public OutputThread(Socket socket,Dialog dialog) throws IOException {
        this.socket = socket;
        InputStream inputStream = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        this.dialog=dialog;
    }
    @Override
    public void run() {

        time.start();
        while (socket.isConnected()){
            time.CheckIfOnline();
            try {
                while (true){
                    line = reader.readLine();
                    if (line!=null){
                        time.resetTime();
                    }
                    dialog.output(line);
                }
            } catch (NullPointerException | IOException e){
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
