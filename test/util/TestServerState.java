package util;

import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import server.ServerState;
import server.kv.MemoryDatabase;

public final class TestServerState {
    public static ServerState create() {
        ServerData sd = new ServerData("name", "localhost", 12345);
        ServerState state = new ServerState(new MemoryDatabase(), sd);
        state.currentServerServerData = sd;
        state.meta = new KVStoreMetaData();
        state.meta.getKvServerList().add(sd);
        state.runningState = RunningState.RUNNING;

        return state;
    }
}
