package server.threads.util.gossip;

import lib.server.TimedRunningStateMap;

import java.util.LinkedList;
import java.util.List;

public class RunningStates {

    public static TimedRunningStateMap combineMaps(TimedRunningStateMap map0, TimedRunningStateMap map1) {
        TimedRunningStateMap result = new TimedRunningStateMap();
        List<String> checkedServers = new LinkedList<>();
        for (String key: map0.getKeys()) {
            checkedServers.add(key);
            if (!map1.hasKey(key))
                result.put(key, map0.get(key));
            else if (map0.get(key).accessTime >= map1.get(key).accessTime)
                result.put(key, map0.get(key));
            else
                result.put(key, map1.get(key));
        }
        for (String key: map1.getKeys()) {
            if (checkedServers.contains(key)) continue;
            result.put(key, map1.get(key));
        }

        return result;
    }
}
