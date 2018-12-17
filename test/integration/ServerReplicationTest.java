package integration;

import client.store.KVStore;
import junit.framework.Assert;
import lib.hash.HashUtil;
import lib.message.AdminMessages.FullReplicationMsg;
import lib.message.KVAdminMessage;
import lib.message.MarshallingException;
import lib.message.Messaging;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.KVServer;
import server.kv.DbProvider;
import server.kv.KeyValueStore;
import server.kv.MemoryDatabase;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class ServerReplicationTest {

    List<KVServer> servers;
    List<ServerData> serverDatas;
    List<KeyValueStore> stores;
    KVStoreMetaData metaData;

    @Before
    public void setup() throws InterruptedException, NoSuchAlgorithmException {
        servers = new LinkedList<>();
        serverDatas = new LinkedList<>();
        stores = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            String serverName = "server" + i;
            ServerData sd = new ServerData(serverName, "localhost", 50000 + i, HashUtil.getHash(serverName));

            KeyValueStore db = new MemoryDatabase();
            KVServer srv = new KVServer(sd, db);
            servers.add(srv);
            serverDatas.add(sd);
            stores.add(db);
        }
        metaData = new KVStoreMetaData();
        metaData.getKvServerList().addAll(serverDatas);
        metaData.getKvServerList().sort(Comparator.comparing(ServerData::getFromHash));

        servers.forEach(s -> {
            s.getState().meta = metaData;
            s.getState().runningState = RunningState.RUNNING;

            new Thread(s).start();
        });

    }

    @After
    public void tearDown() throws Exception {
        servers.stream().parallel().forEach(s -> {
            try {
                s.stop();
            } catch (IOException e) {
            }
        });
    }

    @Test
    public void testFullReplicate() throws KVServerNotFoundException, NoSuchAlgorithmException, MarshallingException, IOException {
        KVStore kvStore = new KVStore(metaData);
        for (int i = 0; i < 100; i++) {
            kvStore.put(String.valueOf(i), String.valueOf(i));
        }
        ServerData sdSource = serverDatas.get(0);
        ServerData sdTarget = metaData.findNextKvServer(metaData.findNextKvServer(metaData.findNextKvServer(sdSource)));
        DbProvider providerSource = servers.stream()
                .filter(s -> s.getState().currentServerServerData == sdSource)
                .findAny().get()
                .getState().dbProvider;
        DbProvider providerTarget = servers.stream()
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
