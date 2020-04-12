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
            int splitLimit = 2;

            String line = "";

            // Try to read the line.
            try {
                line = reader.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (line != null) {
                System.out.println("\tIN\t<< " + line);


                if (line.startsWith("DM") || line.startsWith("GRPMSG") || (line.startsWith("KICK"))) {
                    splitLimit = 3;
                }

                if (line.equals("PONG")) splitLimit = 1;

                String[] incomingMessage = line.split(" ", splitLimit);
                String command = incomingMessage[0];

                if (incomingMessage.length == splitLimit) {

                    switch (command) {
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
                            sendMessage("+OK BCST " + incomingMessage[1]);
                            break;

                        case "QUIT":
                            sendMessage("+OK Goodbye");
                            server.disconnect(username);
                            break;

                        case "RQST":
                            String requested = incomingMessage[1].toLowerCase();
                            if (requested.equals("users")) {
                                server.sendUserList(username);
                            } else if (requested.equals("groups")) {
                                server.sendGroupList(username);
                            }
                            break;

                        case "DM":
                            String recipient = incomingMessage[1];
                            String payload = incomingMessage[2];
                            String returnMessage = "+OK DM " + recipient + " " + payload;
                            String recipientMessage = "DM " + username + " " + payload;
                            server.sendDM(username, recipient, returnMessage, recipientMessage);
                            break;

                        case "MAKE":
                            server.createGroup(username, incomingMessage[1]);
                            break;

                        case "JOIN":
                            server.joinGroup(username, incomingMessage[1]);
                            break;

                        case "GRPMSG":
                            // incomingMessage[1] is the groupname to send the message to.
                            // incomingMessage[2] is the actual message.
                            server.sendGroupMessage(username, incomingMessage[1], incomingMessage[2]);
                            break;

                        case "LEAVE":
                            server.leaveGroup(username, incomingMessage[1]);
                            break;

                        case "KICK":
                            server.kickFromGroup(username, incomingMessage[1], incomingMessage[2]);
                            break;

                        case "PONG":
                            server.notifyPingThread(username);
                            break;

                        default:
                            break;

                    }
                } else {
                    sendMessage("-ERR ARE YOU FUCKING RETARDED LEARN TO PROGRAM A FUCKING CHAT CLIENT WTF MAN HANDLE ERRORS ON YOUR END IM FUCKING LAZY MAN");
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
