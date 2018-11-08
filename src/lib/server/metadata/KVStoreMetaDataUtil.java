package lib.server.metadata;

import lib.server.KVServerNotFoundException;
import lib.server.metadata.KVStoreMetaData;

import java.util.Optional;
import java.util.stream.Collectors;

public class KVStoreMetaDataUtil {

    public static KVStoreMetaData.KVServerMetaData getKVServer(KVStoreMetaData kvStoreMetaData, String key) throws KVServerNotFoundException {
        //todo hash value comparison needs to be changed based on consistent hashing
        int hash = key.hashCode();
        Optional<KVStoreMetaData.KVServerMetaData> kvServer = kvStoreMetaData.getKvServerList().stream().filter(it -> (it.getFromHash() <= hash && hash < it.getToHash())).findAny();

        if(!kvServer.isPresent()) {
            throw new KVServerNotFoundException();
        }

        return kvServer.get();
    }
}
