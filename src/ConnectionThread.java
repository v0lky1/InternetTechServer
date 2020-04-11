import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionThread extends Thread {
    private Server server;
    private ServerSocket serverSocket;

    public ConnectionThread(Server server, int serverPort) throws IOException {
        this.server = server;
        serverSocket = new ServerSocket(serverPort);
    }

    @Override
    public void run() {
        while (true) {
            // Wait for an incoming client-connection request (blocking).
            try {
                Socket socket = serverSocket.accept();
                server.addClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void stopServer() {

    }
}
