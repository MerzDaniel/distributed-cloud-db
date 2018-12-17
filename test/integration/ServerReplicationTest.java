package integration;

import client.store.KVStore;
import junit.framework.Assert;
import lib.message.AdminMessages.FullReplicationMsg;
import lib.message.KVAdminMessage;
import lib.message.MarshallingException;
import lib.message.Messaging;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.ServerData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.kv.DbProvider;
import server.kv.KeyValueStore;
import util.ClusterUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static junit.framework.Assert.assertEquals;

public class ServerReplicationTest {

    ClusterUtil.Cluster cluster = null;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        cluster = ClusterUtil.setupCluster(5);
    }

    @After
    public void tearDown() {
        cluster.shutdown();
        cluster = null;
    }

    @Test
    public void testFullReplicate() throws KVServerNotFoundException, NoSuchAlgorithmException, MarshallingException, IOException {
        KVStore kvStore = new KVStore(cluster.metaData);
        for (int i = 0; i < 100; i++) {
            kvStore.put(String.valueOf(i), String.valueOf(i));
        }
        ServerData sdSource = cluster.serverDatas.get(0);
        ServerData sdTarget = cluster.metaData.findNextKvServer(cluster.metaData.findNextKvServer(cluster.metaData.findNextKvServer(sdSource)));
        DbProvider providerSource = cluster.servers.stream()
                .filter(s -> s.getState().currentServerServerData == sdSource)
                .findAny().get()
                .getState().dbProvider;
        DbProvider providerTarget = cluster.servers.stream()
                .filter(s -> s.getState().currentServerServerData == sdTarget)
                .findAny().get()
                .getState().dbProvider;
        KeyValueStore dbSource_1 = providerSource.getDb(sdSource);
        KeyValueStore dbTarget_1 = providerTarget.getDb(sdSource);

        // data should not be replicated to this server => replicated data of server0 should be zero
        assertEquals(0, dbTarget_1.retrieveAllData().count());

        Messaging messaging = new Messaging();
        messaging.connect(sdSource);
        messaging.sendMessage(new FullReplicationMsg(sdSource.getName(), sdTarget.getName()));
        assertEquals(KVAdminMessage.StatusType.FULL_REPLICATE_SUCCESS, ((KVAdminMessage)messaging.readMessage()).status);

        // now the data should be replicated to this server
        Assert.assertTrue(dbTarget_1.retrieveAllData().count() == dbSource_1.retrieveAllData().count());
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2-test.properties.xml");
    }
}