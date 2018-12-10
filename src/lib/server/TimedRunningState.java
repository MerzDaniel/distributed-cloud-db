package lib.server;

public class TimedRunningState {
    public long accessTime;
    public RunningState runningState;

    public TimedRunningState(long accessTime, RunningState runningState) {
        this.accessTime = accessTime;
        this.runningState = runningState;
    }

    public TimedRunningState(RunningState runningState) {
        this.runningState = runningState;
        this.accessTime = System.currentTimeMillis();
    }
}
