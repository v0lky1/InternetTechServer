import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private PrintWriter writer;
    private BufferedReader reader;
    private Server server;
    private String username;
    private boolean waiting;
    private boolean running;

    public ClientThread(Server server, Socket socket, String tempUsername) throws IOException {
        this.server = server;
        this.socket = socket;
        this.username = tempUsername;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    @Override
    public void run() {
        writer = new PrintWriter(outputStream);
        reader = new BufferedReader(new InputStreamReader(inputStream));

        String welcomeMsg = "HELO " + "Welkom bij RemEd Chatservices!";
        System.err.println("\tOUT\t>> " + welcomeMsg);
        writer.println(welcomeMsg);
        writer.flush();
        //  pt.start();

        running = true;

        while (running) {
            int splitLimit = 3;

            String line = "";

            // Try to read the line.
            try {
                line = reader.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (line != null) {
                System.out.println("\tIN\t<< " + line);


                if (line.startsWith("HELO") || line.startsWith("BCST")) {
                    splitLimit = 2;
                }

                String[] incomingMessage = line.split(" ", splitLimit);

                switch (incomingMessage[0]) {
                    case "HELO":
                        String uname = incomingMessage[1];
                        System.out.println(username);
                        System.out.println(uname);

                        if (!uname.matches("^[a-zA-Z0-9_]{3,}$")) {
                            sendMessage("-ERR username has an invalid format (only characters, numbers and underscores are allowed)");
                        } else if (server.hasUsername(username, uname)) {
                            sendMessage("+OK HELO " + username);
                        } else {
                            sendMessage("-ERR user already logged in");
                        }
                        break;

                    case "BCST":
                        String message = "BCST [" + username + "] " + incomingMessage[1];
                        System.err.println("\tOUT\t>> " + message);
                        server.sendBroadcastMessage(username, message);
                        break;

                    case "QUIT":
                        sendMessage("+OK Goodbye");
                        server.disconnect(username);
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
                        server.notifyPingThread(username);
                        break;

                    default:
                        break;

                }
            }
        }
    }

    public void sendMessage(String message) {
        System.err.println("\tOUT\t>> " + message + " [" + username + "]");
        writer.println(message);
        writer.flush();
    }

    public void exit() {
        running = false;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Socket getSocket() {
        return socket;
    }
}
