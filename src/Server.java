import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private ServerSocket serverSocket;
    private static final int SERVER_PORT = 6969;
    private ArrayList<User> users;

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
            ClientThread ct = new ClientThread(this, socket);
            //creating new user, letting the user know what his clientthread is and we're letting the clientthread
            //know who his user is
            ct.start();
        }
    }

    public void sendBroadcastMessage(String sender, String message) {
        for (User user : users) {
            if (!user.getUsername().equals(sender)) {
                System.err.println("OUT \t >> " + message);
                user.getClientThread().sendMessage(message);
            } else {
                user.getClientThread().sendMessage("+OK " + message);
            }
        }
    }

    public void removeUser(User user) {
        user.disconnect();
        users.remove(user);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public ArrayList<User> getUsers(){
        return users;
    }
}
