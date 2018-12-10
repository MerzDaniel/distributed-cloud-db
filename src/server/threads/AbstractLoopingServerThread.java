package server.threads;

public abstract class AbstractLoopingServerThread extends AbstractServerThread {
    private final long interval;

    public AbstractLoopingServerThread(long interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        logger.info("Start server looping thread: " + this.getClass().getName());
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

    @Override
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
