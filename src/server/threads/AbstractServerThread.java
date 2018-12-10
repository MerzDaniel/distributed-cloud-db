package server.threads;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AbstractServerThread extends Thread {
    protected boolean shouldStop = false;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    public void stopServerThread() {
        shouldStop = true;
        if (!isAlive()) return;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.warn("Interrupted", e);
        }
        this.stop();
    }
}
