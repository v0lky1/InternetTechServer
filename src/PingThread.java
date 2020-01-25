public class PingThread extends Thread {
    private ClientThread ct;
    private boolean pongReceived;

    public PingThread() {

    }

    public void run() {

        while (true) {
            pongReceived = false;
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ct.sendMessage("PING");
            long startTime = System.currentTimeMillis();
            while (!pongReceived) {

                long currentTime = System.currentTimeMillis();
                if (currentTime > (startTime + 3000)) {
                    ct.sendMessage("DSCN Pong timeout");
                    break;
                }
            }
        }
    }

    public void pongReceived() {
        pongReceived = true;
    }

    public void setCt(ClientThread ct) {
        this.ct = ct;
    }
}
