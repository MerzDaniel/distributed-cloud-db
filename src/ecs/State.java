package ecs;

import lib.metadata.KVStoreMetaData;
import lib.server.TimedRunningStateMap;

/**
 * This class keeps the state of the {@link server.KVServer}s
 */
public class State {
    public KVStoreMetaData storeMeta = new KVStoreMetaData();
    public KVStoreMetaData poolMeta = new KVStoreMetaData();
    public TimedRunningStateMap timedRunningStateMap = new TimedRunningStateMap();
    public String sshUsername = "";
}
