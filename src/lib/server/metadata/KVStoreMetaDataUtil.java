package lib.server.metadata;

import lib.server.KVServerNotFoundException;

import java.util.Optional;

public class KVStoreMetaDataUtil {

    public static KVStoreMetaData.MetaContent getKVServer(KVStoreMetaData kvStoreMetaData, String key) throws KVServerNotFoundException {
        //todo hash value comparison needs to be changed based on consistent hashing
        int hash = key.hashCode();
        Optional<KVStoreMetaData.MetaContent> kvServer = kvStoreMetaData.getKvServerList().stream().filter(it -> (it.getFromHash() <= hash && hash < it.getToHash())).findAny();

        if(!kvServer.isPresent()) {
            throw new KVServerNotFoundException();
        }

        return kvServer.get();
    }
}
