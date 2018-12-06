package lib.metadata;

import lib.hash.HashUtil;
import lib.message.MarshallingException;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class keeps the data about {@link server.KVServer} instances that are deployed
 */
public class KVStoreMetaData {
    private final static String RECORD_SEPARATOR = "\u001E";

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
    public ServerData findKVServer(String key) throws KVServerNotFoundException {
        return kvServerList.get(findKvServerIndex(key));
    }

    /**
     * Find the replica {@link server.KVServer}s for the given {@code hash}
     *
     * @param hash hash
     * @return {@link List<ServerData>} list of ServerData representing replica servers
     * @throws KVServerNotFoundException if the {@link server.KVServer} is not found
     */
    public List<ServerData> getReplicaKVServers(BigInteger hash) throws KVServerNotFoundException {
        int noOfServers = kvServerList.size();

        if (noOfServers == 0) throw new KVServerNotFoundException();
        if (noOfServers == 1) return new ArrayList<>();
        if (noOfServers == 2) return Arrays.asList(this.findNextKvServer(hash));

        sortKvServers();

        for (int i = 0; i < kvServerList.size(); i++) {
            if (kvServerList.get(i).getFromHash().compareTo(hash) > 0){
                return kvServerList.subList(i, (i + 3) % noOfServers);
            }
        }
        return kvServerList.subList(0, 2);
    }

    /**
     * Find the next {@link server.KVServer} which has the hash larger than given {@code hash}
     *
     * @param hash hash
     * @return {@link server.KVServer} found larger than {@code hash}
     * @throws KVServerNotFoundException if the {@link server.KVServer} is not found
     */
    public ServerData findNextKvServer(BigInteger hash) throws KVServerNotFoundException {
        if (kvServerList.size() == 0) throw new KVServerNotFoundException();

        sortKvServers();

        for (int i = 0; i < kvServerList.size(); i++) {
            if (kvServerList.get(i).getFromHash().compareTo(hash) > 0) return kvServerList.get(i);
        }
        return kvServerList.get(0);
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

        sortKvServers();
        for (int i = 0; i < kvServerList.size() - 1; i++) {
            if (kvServerList.get(i).getFromHash().compareTo(hash) <= 0
                    && hash.compareTo(kvServerList.get(i + 1).getFromHash()) < 0)
                return i;
        }
        //for the scenario where the hash value of key is larger than the last kvServer(ServerData)
        return kvServerList.size() - 1;
    }

    private void sortKvServers() {
        kvServerList.sort(Comparator.comparing(ServerData::getFromHash));
    }

}
