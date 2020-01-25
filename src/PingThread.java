public class PingThread extends Thread {
    private User user;
    private boolean pongReceived;

    public PingThread() {

    }

    public void run() {

        while (!user.isDisconnect()) {
            pongReceived = false;
            try {
                sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            user.getClientThread().sendMessage("PING");
            long startTime = System.currentTimeMillis();
            while (!pongReceived) {

                long currentTime = System.currentTimeMillis();
                if (currentTime > (startTime + 3000)) {
                    user.getClientThread().sendMessage("DSCN Pong timeout");
                    user.disconnect();
                    break;
                }
            }
        }
    }

    public void pongReceived() {
        pongReceived = true;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
