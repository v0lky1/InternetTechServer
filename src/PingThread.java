public class PingThread extends Thread {

    private String username;
    private Server server;
    private boolean running;
    private long pingSentTime;
    private boolean pongReceived;

    public PingThread(Server server, String username) {
        this.server = server;
        this.username = username;
        this.running = true;
        pongReceived = true;
        pingSentTime = System.currentTimeMillis();
    }

    public void run() {
        while (running) {
            long currentTime = System.currentTimeMillis();
            if (pongReceived) {
                if (currentTime >= (pingSentTime + 18000)) {
                    pongReceived = false;
                    server.sendPing(username);
                    pingSentTime = System.currentTimeMillis();
                }
            } else {

                if (currentTime >= (pingSentTime + 3000)) {
                    server.sendMessage(username, "DSCN Pong timeout");
                    server.disconnect(username);
                    running = false;
                }
            }
        }
    }

    public void exit() {
        running = false;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void receivedPong() {
        pongReceived = true;
    }
}
