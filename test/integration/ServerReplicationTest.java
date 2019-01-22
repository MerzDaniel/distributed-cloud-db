package integration;

import ecs.service.KvService;
import junit.framework.Assert;
import lib.message.admin.KVAdminMessage;
import lib.message.exception.MarshallingException;
import lib.metadata.KVServerNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.KVServer;
import server.kv.KeyValueStore;
import util.ClusterTestUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static junit.framework.Assert.assertEquals;

public class ServerReplicationTest {

    ClusterTestUtil.Cluster cluster = null;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        cluster = ClusterTestUtil.setupCluster(5);
    }

    @After
    public void tearDown() throws InterruptedException {
        cluster.shutdown();
        cluster = null;
        Thread.sleep(500);
    }

    @Test
    public void testFullReplicate() throws KVServerNotFoundException, NoSuchAlgorithmException, MarshallingException, IOException, InterruptedException {
        ClusterTestUtil.fillUpDb(cluster, 200);

        KVServer serverSource = cluster.servers.get(0);
        KVServer serverTarget = cluster.getNextServer(cluster.getNextServer(cluster.getNextServer(serverSource)));
        KeyValueStore dbSource = cluster.getDb(serverSource);
        KeyValueStore replicaTarget = cluster.getReplica(serverTarget, serverSource);

        // data should not be replicated to this server => replicated data of server0 should be zero
        assertEquals(0, replicaTarget.retrieveAllData().count());

        KVAdminMessage replicateResponse= KvService.fullReplicateData(
                cluster.getServerData(serverSource),
                cluster.getServerData(serverSource),
                cluster.getServerData(serverTarget)
        );
        assertEquals(KVAdminMessage.StatusType.FULL_REPLICATE_SUCCESS, (replicateResponse).status);

        // now the data should be replicated to this server
        long replicatedDataCount = replicaTarget.retrieveAllData().count();
        long expectedDataSize = dbSource.retrieveAllData().count();
        Assert.assertEquals(expectedDataSize, replicatedDataCount);
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2-test.properties.xml");
    }
}
