package server.threads;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AbstractServerThread extends Thread {
    private Logger logger = LogManager.getLogger(AbstractServerThread.class);
    private boolean shouldStop = false;
    private final long interval;

    public AbstractServerThread(long interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        while(!shouldStop) {
            loop();
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                logger.warn("Got interrupted: ", e);
                break;
            }
        }
    }

    public void stopServerThread() {
        shouldStop = true;
        try {
            Thread.sleep(Math.min(interval, 100));
        } catch (InterruptedException e) {
            logger.warn("Interrupted", e);
        }
        this.stop();
    }

    protected abstract void loop();
}
