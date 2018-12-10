package server.threads;

import java.util.Random;

public abstract class AbstractLoopingServerThread extends AbstractServerThread {
    private final long minInterval;
    private final long maxInterval;

    public AbstractLoopingServerThread(long interval) {
        minInterval = interval;
        maxInterval = interval;
    }
    public AbstractLoopingServerThread(long minInterval, long maxInterval) {
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
    }

    @Override
    public void run() {
        Random random = new Random();
        logger.info("Start server looping thread: " + this.getClass().getName());
        while(!shouldStop) {
            loop();
            try {
                long sleepTime = minInterval == maxInterval ? minInterval : (long) (minInterval + (random.nextDouble() * (maxInterval - minInterval)));
                Thread.sleep(sleepTime);
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
            Thread.sleep(Math.min(maxInterval, 3000));
        } catch (InterruptedException e) {
            logger.warn("Interrupted", e);
        }
        this.stop();
    }

    protected abstract void loop();
}
