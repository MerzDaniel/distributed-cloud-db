package util;

import lib.hash.HashUtil;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import server.KVServer;
import server.kv.KeyValueStore;
import server.kv.MemoryDatabase;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public final class ClusterUtil {
    public static class Cluster {
        public Cluster(List<KVServer> servers, List<ServerData> serverDatas, List<KeyValueStore> stores, KVStoreMetaData metaData) {
            this.servers = servers;
            this.serverDatas = serverDatas;
            this.stores = stores;
            this.metaData = metaData;
        }

        public List<KVServer> servers;
        public List<ServerData> serverDatas;
        public List<KeyValueStore> stores;
        public KVStoreMetaData metaData;

        public void shutdown() {
            servers.stream().parallel().forEach(s -> {
                try {
                    s.stop();
                } catch (IOException e) {
                }
            });
        }
    }

    public static Cluster setupCluster(int numberOfServers) throws NoSuchAlgorithmException {
        List<KVServer> servers;
        List<ServerData> serverDatas;
        List<KeyValueStore> stores;
        KVStoreMetaData metaData;

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

        return new Cluster(servers, serverDatas, stores, metaData);
    }
}
