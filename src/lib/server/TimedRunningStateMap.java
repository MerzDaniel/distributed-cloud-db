package lib.server;

import lib.Constants;
import lib.message.MarshallingException;

import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

public class TimedRunningStateMap {

    private Map<String, TimedRunningState> map = new Hashtable<>();

    public TimedRunningState get(String serverName) {
        return map.get(serverName);
    }
    public void put(String serverName, TimedRunningState state) {
        map.put(serverName, state);
    }

    public String marshall() {
        return String.join(Constants.RECORD_SEPARATOR,
                map.entrySet().stream().map(
                        x -> x.getKey() + Constants.ELEMENT_SEPARATOR + x.getValue().marshall()
                ).collect(Collectors.toList()));
    }

    public static TimedRunningStateMap unmarshall(String s) throws MarshallingException {
        try {
            TimedRunningStateMap result = new TimedRunningStateMap();
            for (String x : s.split(Constants.RECORD_SEPARATOR)) {
                String[] split = x.split(Constants.ELEMENT_SEPARATOR);
                result.put(split[0], TimedRunningState.unmarshall(split[1] + Constants.ELEMENT_SEPARATOR + split[2]));
            }
            return result;
        } catch(Exception e) {
            throw new MarshallingException(e);
        }
    }
}
