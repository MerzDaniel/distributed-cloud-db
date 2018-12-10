package server.threads;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AbstractServerThread extends Thread {
    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    public void stopServerThread() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            logger.warn("Interrupted", e);
        }
        this.stop();
    }
}
