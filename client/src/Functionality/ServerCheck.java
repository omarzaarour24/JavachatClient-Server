package Functionality;

public class ServerCheck {
    private int seconds = 0;
    private boolean isRunning = false;
    private boolean isCompleted = false;
    private Thread timerThread;

    public void start() {
        if (!isRunning) {
            isRunning = true;
            timerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (seconds < 5) {
                        try {
                            Thread.sleep(1000);
                            seconds++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    isCompleted = true;
                }
            });
            timerThread.start();
        }
    }
    public void resetTime() {
        seconds = 0;
        isCompleted = false;
        //System.out.println("Still Connected.");
    }
    public boolean isCompleted() {
        return isCompleted;
    }

    public void CheckIfOnline() {
        if (isCompleted()){
            System.out.println("Server has disconnected, please try again later");
            System.exit(69);
        }
    }
}
