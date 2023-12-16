package FileTransfer;

import java.io.PrintWriter;

public class Client {
    private String username;
    private String otheruser;
    private PrintWriter writer;

    public Client(String username, String otheruser, PrintWriter writer) {
        this.username = username;
        this.otheruser = otheruser;
        this.writer = writer;
    }

    public String getUsername() {
        return username;
    }


    public String getOtheruser() {
        return otheruser;
    }

}
