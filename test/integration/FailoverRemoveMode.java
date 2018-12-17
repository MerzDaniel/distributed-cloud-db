package integration;

import ecs.service.KvService;
import lib.message.MarshallingException;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import org.apache.logging.log4j.core.jmx.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import server.KVServer;
import server.kv.KeyValueStore;
import util.ClusterTestUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static junit.framework.Assert.assertEquals;

public class FailoverRemoveMode {

    ClusterTestUtil.Cluster cluster = null;

    @Before
    public void setup() throws NoSuchAlgorithmException, MarshallingException, KVServerNotFoundException, IOException {
        cluster = ClusterTestUtil.setupCluster(5);
        cluster.fillUpDb(200);
    }

    @After
    public void tearDown() throws InterruptedException {
        cluster.shutdown();
        cluster = null;
        Thread.sleep(500);
    }

    @Test
    @Ignore
    public void testRemoveFailedNode() throws MarshallingException, KVServerNotFoundException, IOException {
        KVServer sdFailing = cluster.servers.get(0);
        KVServer follower1 = cluster.getNextServer(sdFailing);
        KVServer follower2 = cluster.getNextServer(follower1);
        KVServer follower3 = cluster.getNextServer(follower2);
        KeyValueStore dbOfFailingNode = cluster.getDb(sdFailing);
        KeyValueStore dbOfFollower1 = cluster.getDb(follower1);
        KeyValueStore dbOfFollower3 = cluster.getDb(follower3);

        long amountDataFollower1 = dbOfFollower1.retrieveAllData().count();
        long amountDataFailing = dbOfFailingNode.retrieveAllData().count();
        long amountDataFollower3 = dbOfFollower3.retrieveAllData().count();
        assertEquals(0, cluster.getReplica(follower3, sdFailing).retrieveAllData().count());

        // perform remove Node
        KvService.removeNode(cluster.getServerData(sdFailing), cluster.metaData);


        assertEquals(amountDataFailing + amountDataFollower3, dbOfFollower3.retrieveAllData().count());


    }
    static {
        System.setProperty("log4j.configurationFile", "log4j2-test.properties.xml");
    }

}
