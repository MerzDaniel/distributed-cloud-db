package lib.metadata;

import lib.hash.HashUtil;
import lib.message.MarshallingException;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KVStoreMetaData {
    private final static String RECORD_SEPARATOR = "\u001E";

    private List<MetaContent> kvServerList = new ArrayList<>();

    public KVStoreMetaData(List<MetaContent> kvServerList) {
        this.kvServerList = kvServerList;
    }

    public KVStoreMetaData() {

    }

    public List<MetaContent> getKvServerList() {
        return kvServerList;
    }

    public String marshall() {
        return getKvServerList().stream().map(it -> it.marshall()).collect(Collectors.joining(RECORD_SEPARATOR));
    }

    public static KVStoreMetaData unmarshall(String kvStoreMetaData) throws MarshallingException {
        List<MetaContent> metaContentList = new ArrayList<>();
        String[] kvServers = kvStoreMetaData.split(RECORD_SEPARATOR);

        for (String kvServer : kvServers) {
            MetaContent metaContent = MetaContent.unmarshall(kvServer);
            metaContentList.add(metaContent);
        }

        return new KVStoreMetaData(metaContentList);
    }

    public MetaContent findKVServer(String key) throws NoSuchAlgorithmException, KVServerNotFoundException {
        BigInteger hash = HashUtil.getHash(key);

        if (kvServerList == null || kvServerList.size() == 0) {
            throw new KVServerNotFoundException();
        }

        if (kvServerList.size() == 1) return kvServerList.get(0);

        for (int i = 0; i < kvServerList.size() - 1; i++) {
            if (kvServerList.get(i).getFromHash().compareTo(hash) < 0
                    && hash.compareTo(kvServerList.get(i + 1).getFromHash()) < 0)
                return kvServerList.get(i);
        }
        //for the scenario where the hash value of key is larger than the last kvServer(MetaContent)
        return kvServerList.get(kvServerList.size() - 1);
    }

}
