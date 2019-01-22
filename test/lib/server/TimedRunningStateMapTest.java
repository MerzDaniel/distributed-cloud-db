package lib.server;

import lib.message.exception.MarshallingException;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TimedRunningStateMapTest {

    @Test
    public void testMarshallUnmarshall() throws MarshallingException {
        TimedRunningStateMap timedRunningStateMap = new TimedRunningStateMap();
        timedRunningStateMap.put("server key", new TimedRunningState(1111, RunningState.RUNNING));
        timedRunningStateMap.put("server key 2", new TimedRunningState(2222, RunningState.UNCONFIGURED));

        TimedRunningStateMap result = TimedRunningStateMap.unmarshall(timedRunningStateMap.marshall());
        assertEquals(1111, result.get("server key").accessTime);
        assertEquals(RunningState.RUNNING, result.get("server key").runningState);
        assertEquals(2222, result.get("server key 2").accessTime);
        assertEquals(RunningState.UNCONFIGURED, result.get("server key 2").runningState);
    }
}
