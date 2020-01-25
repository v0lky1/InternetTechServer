import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private ServerSocket serverSocket;
    private static final int SERVER_PORT = 6969;
    private static ArrayList<User> users;

    public static void main(String[] args) {
        try {
            new Server().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() throws IOException {
        // Create a socket to wait for clients.
        serverSocket = new ServerSocket(SERVER_PORT);
        users = new ArrayList<>();

        while (true) {
            // Wait for an incoming client-connection request (blocking).
            Socket socket = serverSocket.accept();
            PingThread pt = new PingThread();
            ClientThread ct = new ClientThread(this, socket, pt);


            //creating new user, letting the user know what his clientthread is and we're letting the clientthread
            //know who his user is
            User user = new User(ct);
            users.add(user);
            ct.setUser(user);
            ct.start();
        }
    }

    public void sendBroadcastMessage(String sender, String message) {
        for (User user : users) {
            if (!user.getUsername().equals(sender)) {
                user.getClientThread().sendMessage(message);
            }
        }
    }

    public void removeUser(User user) {
        user.disconnect();
        users.remove(user);
    }
}
