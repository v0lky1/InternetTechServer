import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private PrintWriter writer;
    private BufferedReader reader;
    private Server server;
    private User user;

    public ClientThread(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        writer = new PrintWriter(outputStream);
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public void run() {
        StringBuilder sb = new StringBuilder().append("HELO ");
        String welcomeMessage = "Welkom bij RemEd Chatservices!";
        sb.append(welcomeMessage);
        System.out.println(sb.toString());
        writer.println(sb.toString());
        writer.flush();

        while (!user.isDisconnect()) {
            int splitLimit = 3;

            String line = null;

            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (line != null) {

                System.out.println("IN " + user + " " + line);

                if (line.startsWith("HELO") || line.startsWith("BCST")) {
                    splitLimit = 2;
                }

                String[] incomingMessage = line.split(" ", splitLimit);

                switch (incomingMessage[0]) {
                    case "HELO":
                        user.setUsername(incomingMessage[1]);
                        sendMessage("+OK HELO " + user.getUsername());
                        break;

                    case "BCST":
                        String message = "BCST [" + user.getUsername() + "] " + incomingMessage[1];
                        System.out.println("OUT " + message);
                        server.sendBroadcastMessage(user.getUsername(), message);
                        break;

                    case "QUIT":
                        sendMessage("+ OK Goodbye");
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        user.disconnect();
                        break;

                    case "RQST":
                        break;

                    case "DM":
                        break;

                    case "MAKE":
                        break;

                    case "JOIN":
                        break;

                    case "GRPMSG":
                        break;

                    case "LEAVE":
                        break;

                    case "PONG":
                        user.getPingThread().pongReceived();
                        break;

                    default:
                        break;

                }
            }
        }
    }

    public void sendMessage(String message) {
        System.out.println("OUT " + message + " " + user);
        writer.println(message);
        writer.flush();
    }

    public void setUser(User user) {
        this.user = user;
    }
}
