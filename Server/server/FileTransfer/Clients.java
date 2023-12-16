package FileTransfer;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Clients{
    private Socket sender;
    private Socket receiver;
    private boolean BothHere;
    private  Map<Client, Socket> connected = new HashMap<>();

    public void setSender(Socket sender, Client client) {
        this.sender = sender;
        connected.put(client, sender);
        for (Client c : connected.keySet()) {
            if (c.getUsername().equals(client.getOtheruser())) {
                BothHere = true;
                break; // stop the loop if a match is found
            }
        }
    }

    public void setReceiver(Socket receiver,Client client) {
        this.receiver = receiver;
        connected.put(client,receiver);
        for (Client c : connected.keySet()) {
            if (c.getUsername().equals(client.getOtheruser())) {
                BothHere = true;
                break; // stop the loop if a match is found
            }
        }
    }
    public Socket getReceiver(String username) {
        Socket socket1 = null;
        for (Client c: connected.keySet()){
            if (c.getUsername().equals(username)){
                socket1=connected.get(c);
            }
        }
        return socket1;
    }

    public Socket getSender(String username) {
        Socket socket1 = null;
        for (Client c: connected.keySet()){
            if (c.getUsername().equals(username)){
                socket1=connected.get(c);
            }
        }
        return socket1;
    }
    public boolean isBotHere() {
        return BothHere;
    }

}
