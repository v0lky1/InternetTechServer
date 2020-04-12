import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private static final int SERVER_PORT = 6969;
    private ArrayList<String> usernames;
    private HashMap<String, ClientThread> clientThreads;
    private HashMap<String, PingThread> pingThreads;
    private ArrayList<Group> groups;
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
        groups = new ArrayList<>();
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

    public void sendDM(String sender, String recipient, String returnMessage, String message) {
        if (validRecipient(recipient)) {
            clientThreads.get(recipient).sendMessage(message);
            clientThreads.get(sender).sendMessage(returnMessage);
        } else {
            clientThreads.get(sender).sendMessage("-ERR user not online");
        }
    }

    public void sendUserList(String username) {
        clientThreads.get(username).sendMessage("+OK users");
        for (String user : usernames) {
            clientThreads.get(username).sendMessage("+USR " + user);
        }
        clientThreads.get(username).sendMessage("+EOL users");
    }

    public void sendGroupList(String username) {
        if (groups.size() >= 1) {
            clientThreads.get(username).sendMessage("+OK groups");
            for (Group group : groups) {
                clientThreads.get(username).sendMessage("+GRP " + group.getGroupName());
            }
            clientThreads.get(username).sendMessage("+EOL groups");
        } else {
            clientThreads.get(username).sendMessage("-ERR no groups on the server.");
        }
    }

    public void createGroup(String owner, String groupName) {
        groupName = groupName.toLowerCase();
        if (!groupExists(groupName)) {
            groups.add(new Group(owner, groupName));
            clientThreads.get(owner).sendMessage("+OK MAKE " + groupName);
        } else {
            clientThreads.get(owner).sendMessage("-ERR group name already exists");
        }
    }

    public void joinGroup(String username, String groupName) {
        String message = "";
        Group group = getGroupByName(groupName);

        if (group != null) {
            if (group.addMember(username)) {
                message = "+OK JOIN " + groupName;
            } else {
                message = "-ERR you're already part of this group.";
            }
        } else {
            message = "-ERR group does not exist.";
        }
        clientThreads.get(username).sendMessage(message);
    }

    public void leaveGroup(String username, String groupName) {
        String response = "";
        Group ourGroup = getGroupByName(groupName);

        if (ourGroup != null) {
            if (ourGroup.isMember(username)) {
                response = "+OK LEAVE " + groupName;
                if (ourGroup.getOwner().equals(username)) {
                    groups.remove(ourGroup);
                    response = "+OK REMOVED " + groupName;
                } else {
                    ourGroup.removeMember(username);
                }
            } else {
                response = "-ERR you are not a part of this group.";
            }
        } else {
            response = "-ERR group does not exist.";
        }

        clientThreads.get(username).sendMessage(response);
    }

    public void kickFromGroup(String kicker, String groupName, String kickee) {
        String response = "";
        Group group = getGroupByName(groupName);

        if (group != null) {
            if (group.isMember(kickee)) {
                if (group.getOwner().equals(kicker)) {
                    if (!kicker.equals(kickee)) {
                        response = "+OK KICK " + groupName + " " + kickee;
                        group.removeMember(kickee);
                        clientThreads.get(kickee).sendMessage("REMOVED " + groupName);
                    } else {
                        response = "-ERR cannot kick yourself from a group.";
                    }
                } else {
                    response = "-ERR you are not the owner of this group.";
                }
            } else {
                response = "-ERR user is not part of this group.";
            }
        } else {
            response = "-ERR group does not exist.";
        }

        clientThreads.get(kicker).sendMessage(response);
    }

    public Group getGroupByName(String groupName) {
        if (groupExists(groupName)) {
            for (Group group : groups) {
                if (group.getGroupName().equals(groupName)) {
                    return group;
                }
            }
        }
        return null;
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

    public void sendGroupMessage(String username, String groupName, String message) {
        String response = "";
        if (groupExists(groupName)) {
            for (Group group : groups) {
                if (group.getGroupName().equals(groupName)) {
                    System.out.println("we hebben de group bois.");
                    if (group.isMember(username)) {
                        response = "+OK GRPMSG " + groupName + " " + message;
                        ArrayList<String> members = group.getMembers();
                        System.out.println(members.toString());
                        for (String member : members) {
                            if (!member.equals(username)) {
                                clientThreads.get(member).sendMessage("GRPMSG " + groupName + " " + username + " " + message);
                            }
                        }
                    } else {
                        response = "-ERR not a part of this group.";
                    }
                }
            }
        } else {
            response = "-ERR group does not exist.";
        }
        clientThreads.get(username).sendMessage(response);
    }

    public boolean validRecipient(String username) {
        for (String user : usernames) {
            if (user.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean groupExists(String groupName) {
        for (Group group : groups) {
            if (group.getGroupName().equals(groupName)) {
                return true;
            }
        }
        return false;
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
