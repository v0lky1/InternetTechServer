public class PingThread extends Thread {
    private ClientThread ct;
    private boolean running;

    public PingThread(ClientThread ct) {
        this.ct = ct;
    }

    public void run() {
        running = true;

        while (running) {
            try {
                sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ct.sendMessage("PING");
            System.out.println("PING: " + ct.getUser());

            long pingSentTime = System.currentTimeMillis();
            ct.pingSent(pingSentTime);
        }
    }

    public void exit() {
        running = false;
    }
}
