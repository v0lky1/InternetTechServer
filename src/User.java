import java.io.IOException;

public class User {
    private String username;
    private ClientThread ct;
    private PingThread pt;

    public User(ClientThread ct, PingThread pt) {
        this.ct = ct;
        this.pt = pt;
    }

    public ClientThread getClientThread() {
        return ct;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void disconnect() {
        pt.exit();
        ct.exit();
        try {
            ct.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("Dropped user: " + username);
    }

    public PingThread getPingThread() {
        return pt;
    }

    @Override
    public String toString() {
        return username;
    }
}
