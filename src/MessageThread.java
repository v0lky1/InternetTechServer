import java.io.*;
import java.net.Socket;

public class MessageThread extends Thread {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private PrintWriter writer;
    private BufferedReader reader;
    private Server server;

    private String username;

    public MessageThread(Server server, Socket socket) throws IOException {
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

        while (true) {
            int splitLimit = 3;

            String line = null;

            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (line != null) {

                System.out.println("INC " + line);

                if (line.startsWith("HELO") || line.startsWith("BCST")) {
                    splitLimit = 2;
                }

                String[] incomingMessage = line.split(" ", splitLimit);

                switch (incomingMessage[0]) {
                    case "HELO":
                        this.username = incomingMessage[1];
                        sendMessage("+OK HELO " + username);
                        break;

                    case "BCST":
                        String message = "BCST [" + username + "] " + incomingMessage[1];
                        System.out.println("OUT " + message);
                        server.sendBroadcastMessage(username, message);
                        break;

                    case "QUIT":
                        sendMessage("+ OK Goodbye");
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.removeConnection(this);
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
                        // Check time we sent the ping
                        // if within 3 seconds, all good.
                        // else drop everything.
                        break;

                    default:
                        break;

                }
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String message) {
        System.out.println("OUT" + message);
        writer.println(message);
        writer.flush();
    }
}
