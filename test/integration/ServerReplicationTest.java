package integration;

import ecs.service.KvService;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.junit.After;
import org.junit.Before;
import server.KVServer;

import java.util.Arrays;

public class ServerReplicationTest {
    KVServer server0;
    KVServer server1;
    ServerData serverData0;
    ServerData serverData1;

    @Before
    public void setup() {
        KVStoreMetaData meta = new KVStoreMetaData();
        serverData0 = new ServerData("server0", "localhost", 50000);
        serverData1 = new ServerData("server1", "localhost", 50001);
        meta.getKvServerList().addAll(Arrays.asList(
                serverData0, serverData1
        ));
        server0 = new KVServer(serverData0);
        server1 = new KVServer(serverData1);
        server0.run();
        server1.run();
    }
    @After
    public void tearDown() throws Exception {
        server0.stop();
        server1.stop();
    }

}
