package server.threads.util.gossip;

import lib.server.TimedRunningStateMap;

import java.util.LinkedList;
import java.util.List;

public class RunningStates {

    public static TimedRunningStateMap combineMaps(TimedRunningStateMap map0, TimedRunningStateMap map1) {
        TimedRunningStateMap result = new TimedRunningStateMap();

        combineMapsInto(result, map0, map1);

        return result;
    }

    public static void combineMapsInto(TimedRunningStateMap targetMap, TimedRunningStateMap map0, TimedRunningStateMap map1) {
        List<String> checkedServers = new LinkedList<>();
        for (String key: map0.getKeys()) {
            checkedServers.add(key);
            if (!map1.hasKey(key))
                targetMap.put(key, map0.get(key));
            else if (map0.get(key).accessTime >= map1.get(key).accessTime)
                targetMap.put(key, map0.get(key));
            else
                targetMap.put(key, map1.get(key));
        }
        for (String key: map1.getKeys()) {
            if (checkedServers.contains(key)) continue;
            targetMap.put(key, map1.get(key));
        }
    }
}
