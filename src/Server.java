import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private static final int SERVER_PORT = 6969;
    private ArrayList<String> usernames;
    private HashMap<String, ClientThread> clientThreads;
    private HashMap<String, PingThread> pingThreads;
    private int userCounter;

    public static void main(String[] args) {
        try {
            new Server().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() throws IOException {
        // Creates a thread that will listen to new connections so the main Server thread does not block.
        userCounter = 0;
        ConnectionThread connectionThread = new ConnectionThread(this, SERVER_PORT);
        usernames = new ArrayList<>();
        clientThreads = new HashMap<>();
        pingThreads = new HashMap<>();

        connectionThread.start();
    }

    public void sendBroadcastMessage(String sender, String message) {
        for (String user : usernames) {
            if (!user.equals(sender)) {
                clientThreads.get(user).sendMessage(message);
            }
        }
    }

    public void sendPing(String username) {
        clientThreads.get(username).sendMessage("PING");
    }

    public void notifyPingThread(String username) {
        pingThreads.get(username).receivedPong();
    }

    public void sendMessage(String username, String message) {
        clientThreads.get(username).sendMessage(message);
    }

    public void addClient(Socket socket) {
        ClientThread ct = null;
        PingThread pt = null;

        // generating a temporary username, since we do not know the username the user will use.
        // Format: '&&' + unique number
        // Reason: '&&' is not allowed in a username, so a temporary username can never be flagged as "in-use"
        String tempUsername = "&&" + userCounter++;
        try {
            ct = new ClientThread(this, socket, tempUsername);
            pt = new PingThread(this, tempUsername);
            ct.start();
            pt.start();
            clientThreads.put(tempUsername, ct);
            pingThreads.put(tempUsername, pt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean hasUsername(String oldUsername, String username) {
        for (String u : usernames) {
            if (u.toLowerCase().equals(username.toLowerCase())) {
                return false;
            }
        }
        // The username is available, add this user to the list and replace the key in hashmaps.
        usernames.add(username);
        ClientThread ct = clientThreads.remove(oldUsername);
        PingThread pt = pingThreads.remove(oldUsername);
        clientThreads.put(username, ct);
        pingThreads.put(username, pt);

        // Set the usernames in the client and ping threads so they can use it.
        ct.setUsername(username);
        pt.setUsername(username);
        return true;
    }

    public void disconnect(String username) {
        usernames.remove(username);
        ClientThread ct = clientThreads.remove(username);
        ct.exit();
        PingThread pt = pingThreads.remove(username);
        pt.exit();
    }
}
