package lib.server;

import lib.Constants;
import lib.message.MarshallingException;

public class TimedRunningState {
    public TimedRunningState(long accessTime, RunningState runningState) {
        this.accessTime = accessTime;
        this.runningState = runningState;
    }

    public long accessTime;
    public RunningState runningState;

    public String marshall() {
        return String.valueOf(accessTime) + Constants.ELEMENT_SEPARATOR + runningState.name();
    }

    public static TimedRunningState unmarshall(String s) throws MarshallingException {
        String[] split = s.split(Constants.ELEMENT_SEPARATOR);
        try {
            return new TimedRunningState(Long.valueOf(split[0]).longValue(), RunningState.valueOf(split[1]));
        } catch (Exception e) {
            throw new MarshallingException(e);
        }
    }
}
