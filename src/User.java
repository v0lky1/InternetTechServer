public class User {
    private String username;
    private ClientThread ct;
    private PingThread pt;
    private boolean disconnect = false;

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

    public boolean isDisconnect() {
        return disconnect;
    }

    public void disconnect() {
        System.err.println("Dropping: " + username);
        this.disconnect = true;
    }

    public PingThread getPingThread() {
        return pt;
    }

    @Override
    public String toString() {
        return username;
    }
}
