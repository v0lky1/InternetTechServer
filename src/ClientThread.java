import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private PrintWriter writer;
    private BufferedReader reader;
    private Server server;
    private PingThread pt;
    private User user;
    private boolean waiting;
    private boolean running;
    private boolean userAlreadyLoggedIn = false;
    private boolean invalidCharacters = true;

    public ClientThread(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.pt = new PingThread(this);
    }

    @Override
    public void run() {
        writer = new PrintWriter(outputStream);
        reader = new BufferedReader(new InputStreamReader(inputStream));

        String welcomeMsg = "HELO " + "Welkom bij RemEd Chatservices!";
        System.err.println("OUT \t >> " + welcomeMsg);
        writer.println(welcomeMsg);
        writer.flush();
        //  pt.start();

        running = true;

        while (running) {
            int splitLimit = 3;

            String line = "";


            try {
                line = reader.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (line != null) {
                System.out.println("IN \t << " + user + " " + line);


                if (line.startsWith("HELO") || line.startsWith("BCST")) {
                    splitLimit = 2;
                }

                String[] incomingMessage = line.split(" ", splitLimit);



                switch (incomingMessage[0]) {
                    case "HELO":
                        String username = incomingMessage[1];
                        System.out.println(username);

                        for (User u : server.getUsers()) {
                            if (u.getUsername().matches("^[a-zA-Z0-9._-]{3,}$")) {
                                invalidCharacters = false;
                            }
                            if (u.getUsername().equals(username)) {
                                userAlreadyLoggedIn = true;
                            }
                        }
                        if (userAlreadyLoggedIn) {
                            sendMessage("-ERR user already logged in");
                        } else if(invalidCharacters) {
                            sendMessage("-ERR username has an invalid format (only characters, numbers and underscores are allowed)");
                        }
                            server.addUser(new User(this, this.pt, username));
                            sendMessage("+OK HELO " + username);
                        break;

                    case "BCST":
                        String message = "BCST [" + user.getUsername() + "] " + incomingMessage[1];
                        System.err.println("OUT \t >> " + message);
                        server.sendBroadcastMessage(user.getUsername(), message);
                        break;

                    case "QUIT":
                        sendMessage("+OK Goodbye");
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
                        waiting = false;
                        break;

                    default:
                        break;

                }
            }

        }
    }

    public void sendMessage(String message) {
        System.err.println("OUT \t >> " + message + " " + user);
        writer.println(message);
        writer.flush();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void exit() {
        running = false;
    }

    public Socket getSocket() {
        return socket;
    }

    public void pingSent(long pingSentTime) {
        waiting = true;
//todo WIPd
        while (waiting) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= (pingSentTime + 3 * 1000)) {
                sendMessage("DSCN Pong timeout");
                user.disconnect();
                break;
            }
        }
    }
}
