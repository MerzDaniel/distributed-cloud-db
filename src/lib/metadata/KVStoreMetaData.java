package lib.metadata;

import lib.hash.HashUtil;
import lib.message.MarshallingException;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static lib.Constants.RECORD_SEPARATOR;

/**
 * This class keeps the data about {@link server.KVServer} instances that are deployed
 */
public class KVStoreMetaData {

    private List<ServerData> kvServerList = new ArrayList<>();

    /**
     * Create a {@link KVStoreMetaData} instance
     *
     * @param kvServerList list of {@link server.KVServer} instances
     */
    public KVStoreMetaData(List<ServerData> kvServerList) {
        this.kvServerList = kvServerList;
    }

    /**
     * Create a {@link KVStoreMetaData} instance
     */
    public KVStoreMetaData() {

    }

    /**
     * Get the details about {@link server.KVServer} instances
     *
     * @return {@Link ServerData} list
     */
    public List<ServerData> getKvServerList() {
        return kvServerList;
    }

    /**
     * Marshall this {@link KVStoreMetaData} instance
     *
     * @return marshalled string
     */
    public String marshall() {
        return getKvServerList().stream().map(it -> it.marshall()).collect(Collectors.joining(RECORD_SEPARATOR));
    }

    /**
     * Unmarshall the {@code kvStoreMetaData} string to a {@link KVStoreMetaData} instance
     *
     * @param kvStoreMetaData string to be unmarshalled
     * @return {@Link KVStoreMetaData} instance
     * @throws MarshallingException if any exception happens during unmarshalling
     */
    public static KVStoreMetaData unmarshall(String kvStoreMetaData) throws MarshallingException {
        List<ServerData> serverDataList = new ArrayList<>();
        String[] kvServers = kvStoreMetaData.split(RECORD_SEPARATOR);

        for (String kvServer : kvServers) {
            ServerData serverData = ServerData.unmarshall(kvServer);
            serverDataList.add(serverData);
        }

        return new KVStoreMetaData(serverDataList);
    }

    /**
     * Find the {@Link ServerData} for given {@code key}
     *
     * @param key string key
     * @return {@Link ServerData}
     * @throws KVServerNotFoundException if the {@link server.KVServer} is not found
     */
    public ServerData findKVServerForKey(String key) throws KVServerNotFoundException {
        return kvServerList.get(findKvServerIndex(key));
    }

    public ServerData findKvServerByName(String serverName) throws KVServerNotFoundException {
        for (ServerData sd : kvServerList) {
            if (sd.getName().equals(serverName))
                return sd;
        }
        throw new KVServerNotFoundException();
    }

    /**
     * Find the replica {@link server.KVServer}s for the given {@code key} which are immediate KVServers in the hash ring with larger hash
     *
     * @param key Key
     * @return {@link List<ServerData>} list of ServerData representing replica servers
     * @throws KVServerNotFoundException if the {@link server.KVServer} is not found
     */
    public List<ServerData> findReplicaKVServers(String key) throws KVServerNotFoundException {
        int noOfServers = kvServerList.size();
        BigInteger hash = null;
        try {
            hash = HashUtil.getHash(key);
        } catch (NoSuchAlgorithmException e) {
            throw new KVServerNotFoundException();
        }

        if (noOfServers == 0) throw new KVServerNotFoundException();
        if (noOfServers == 1) return new ArrayList<>();
        if (noOfServers == 2) return Arrays.asList(this.findNextKvServerByHash(hash));

        for (int i = 0; i < kvServerList.size(); i++) {
            if (kvServerList.get(i).getFromHash().compareTo(hash) > 0){
                return Arrays.asList(kvServerList.get(i), kvServerList.get((i + 1) % noOfServers));
            }
        }
        return kvServerList.subList(0, 2);
    }

    /**
     * Find the replicated {@link server.KVServer}s by this server (previous two servers)
     *
     * @param serverData {@link ServerData}
     * @return {@link List<ServerData>} list of ServerData representing replica servers
     * @throws KVServerNotFoundException if the {@link server.KVServer} is not found
     */
    public List<ServerData> findReplicatedKVServersBy(ServerData serverData) throws KVServerNotFoundException {

        int noOfServers = kvServerList.size();

        if (noOfServers == 0) throw new KVServerNotFoundException();
        if (noOfServers == 1) return new ArrayList<>();
        if (noOfServers == 2) return Arrays.asList(this.findPreviousKvServer(serverData));

        for (int i = noOfServers - 1; i == 0; i--) {
            if (kvServerList.get(i).getFromHash().compareTo(serverData.getFromHash()) < 0){
                if (i == 0) return Arrays.asList(kvServerList.get(0), kvServerList.get(noOfServers - 1));
                return kvServerList.subList(i - 1, i + 1);
            }
        }
        return kvServerList.subList(noOfServers - 2, noOfServers);
    }

    /**
     * Find the next {@link server.KVServer} which has the hash larger than given {@code hash}
     *
     * @param hash hash
     * @return {@link server.KVServer} found larger than {@code hash}
     * @throws KVServerNotFoundException if the {@link server.KVServer} is not found
     */
    public ServerData findNextKvServerByHash(BigInteger hash) throws KVServerNotFoundException {
        if (kvServerList.size() == 0) throw new KVServerNotFoundException();

        for (int i = 0; i < kvServerList.size(); i++) {
            if (kvServerList.get(i).getFromHash().compareTo(hash) > 0) return kvServerList.get(i);
        }
        return kvServerList.get(0);
    }

    /**
     * Find the previous {@link server.KVServer} which has the hash smaller than given {@code hash}
     *
     * @param serverData {@link ServerData}
     * @return {@link ServerData} found larger than {@code hash}
     * @throws KVServerNotFoundException if the {@link ServerData} is not found
     */
    public ServerData findPreviousKvServer(ServerData serverData) throws KVServerNotFoundException {
        int noOfServers = kvServerList.size();
        if (noOfServers == 0 || noOfServers == 1) throw new KVServerNotFoundException();

        for (int i = noOfServers - 1; i == 0; i--) {
            if (kvServerList.get(i).getFromHash().compareTo(serverData.getFromHash()) < 0) return kvServerList.get(i);
        }
        return kvServerList.get(noOfServers - 1);
    }
    public ServerData findNextKvServer(ServerData serverData) throws KVServerNotFoundException {
        int index = kvServerList.indexOf(serverData);
        return kvServerList.get((index + 1) % kvServerList.size());
    }

    private int findKvServerIndex(String key) throws KVServerNotFoundException {
        BigInteger hash;
        try {
            hash = HashUtil.getHash(key);
        } catch (NoSuchAlgorithmException e) {
            throw new KVServerNotFoundException();
        }

        if (kvServerList == null || kvServerList.size() == 0) {
            throw new KVServerNotFoundException();
        }

        if (kvServerList.size() == 1) return 0;

        for (int i = 0; i < kvServerList.size() - 1; i++) {
            if (kvServerList.get(i).getFromHash().compareTo(hash) <= 0
                    && hash.compareTo(kvServerList.get(i + 1).getFromHash()) < 0)
                return i;
        }
        //for the scenario where the hash value of key is larger than the last kvServer(ServerData)
        return kvServerList.size() - 1;
    }
}
