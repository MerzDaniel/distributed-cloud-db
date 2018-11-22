package lib.metadata;

import lib.hash.HashUtil;
import lib.message.MarshallingException;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KVStoreMetaData {
    private final static String RECORD_SEPARATOR = "\u001E";

    private List<ServerData> kvServerList = new ArrayList<>();

    public KVStoreMetaData(List<ServerData> kvServerList) {
        this.kvServerList = kvServerList;
    }

    public KVStoreMetaData() {

    }

    public List<ServerData> getKvServerList() {
        return kvServerList;
    }

    public String marshall() {
        return getKvServerList().stream().map(it -> it.marshall()).collect(Collectors.joining(RECORD_SEPARATOR));
    }

    public static KVStoreMetaData unmarshall(String kvStoreMetaData) throws MarshallingException {
        List<ServerData> serverDataList = new ArrayList<>();
        String[] kvServers = kvStoreMetaData.split(RECORD_SEPARATOR);

        for (String kvServer : kvServers) {
            ServerData serverData = ServerData.unmarshall(kvServer);
            serverDataList.add(serverData);
        }

        return new KVStoreMetaData(serverDataList);
    }

    public ServerData findKVServer(String key) throws KVServerNotFoundException {
        return kvServerList.get(findKvServerIndex(key));
    }

    public ServerData findNextKvServer(BigInteger hash) throws KVServerNotFoundException {
        if (kvServerList.size() <= 1) throw new KVServerNotFoundException();

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

        kvServerList.sort(Comparator.comparing(ServerData::getFromHash));
        for (int i = 0; i < kvServerList.size() - 1; i++) {
            if (kvServerList.get(i).getFromHash().compareTo(hash) <= 0
                    && hash.compareTo(kvServerList.get(i + 1).getFromHash()) < 0)
                return i;
        }
        //for the scenario where the hash value of key is larger than the last kvServer(ServerData)
        return kvServerList.size() - 1;
    }

}
