package lib.server;

import lib.Constants;
import lib.message.MarshallingException;

import java.util.*;
import java.util.stream.Collectors;

public class TimedRunningStateMap {

    private Map<String, TimedRunningState> map = new Hashtable<>();

    public TimedRunningState get(String serverName) {
        return map.get(serverName);
    }
    public void put(String serverName, TimedRunningState state) {
        map.put(serverName, state);
    }
    public Set<String> getKeys() { return map.keySet(); }
    public boolean hasKey(String key) { return map.get(key) != null; }

    public String marshall() {
        return String.join(Constants.RECORD_SEPARATOR,
                map.entrySet().stream().map(
                        x -> String.join(Constants.ELEMENT_SEPARATOR, x.getKey(), String.valueOf(x.getValue().accessTime), x.getValue().runningState.name())
                ).collect(Collectors.toList()));
    }

    public static TimedRunningStateMap unmarshall(String s) throws MarshallingException {
        try {
            TimedRunningStateMap result = new TimedRunningStateMap();
            if (s.equals("")) return result;

            for (String x : s.split(Constants.RECORD_SEPARATOR)) {
                String[] split = x.split(Constants.ELEMENT_SEPARATOR);
                if (split.length != 3) throw new MarshallingException("hasTheLargeHadronColliderDestroyedTheWorldYet.com => Yup");
                result.put(split[0], new TimedRunningState(Long.parseLong(split[1]), RunningState.valueOf(split[2])));
            }
            return result;
        } catch(Exception e) {
            throw new MarshallingException(e);
        }
    }
}
