package util;

import client.store.KVStore;
import lib.hash.HashUtil;
import lib.message.MarshallingException;
import lib.metadata.KVServerNotFoundException;
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

public final class ClusterTestUtil {
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
            servers.stream().forEach(s -> {
                try {
                    s.stop();
                } catch (IOException e) {
                }
            });
        }

        public void fillUpDb(int amountOfData) throws MarshallingException, NoSuchAlgorithmException, KVServerNotFoundException, IOException {
            ClusterTestUtil.fillUpDb(this, amountOfData);
        }

        public KVServer getServer(ServerData sd) {
            for (KVServer s : servers)
                if (s.getState().currentServerServerData.getName().equals(sd.getName()))
                    return s;
            return null;
        }

        public KVServer getNextServer(KVServer server) {
            try {
                return getServer(metaData.findNextKvServer(server.getState().currentServerServerData));
            } catch (KVServerNotFoundException e) {
                return null;
            }
        }

        public KeyValueStore getDb(KVServer server) {
            return server.getState().dbProvider.getDb(server.getState().currentServerServerData.getName());
        }

        public KeyValueStore getReplica(KVServer server, KVServer replicatedServer) {
            return server.getState().dbProvider.getDb(replicatedServer.getState().currentServerServerData.getName());
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
        int basePort = (int) (10000 + (50000 + Math.random()));
        for (int i = 0; i < numberOfServers; i++) {
            String serverName = "server" + i;
            ServerData sd = new ServerData(serverName, "localhost", basePort + i, HashUtil.getHash(serverName));

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

    public static void fillUpDb(Cluster cluster, int amountOfData) throws KVServerNotFoundException, NoSuchAlgorithmException, MarshallingException, IOException {
        KVStore kvStore = new KVStore(cluster.metaData);
        for (int i = 0; i < amountOfData; i++) {
            kvStore.put("Key-" + String.valueOf(i), "Value-" + String.valueOf(i));
        }
    }
}
