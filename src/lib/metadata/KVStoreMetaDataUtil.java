package lib.metadata;

import java.util.List;
import java.util.Optional;

public class KVStoreMetaDataUtil {

    public static MetaContent getKVServer(KVStoreMetaData kvStoreMetaData, String key) throws KVServerNotFoundException {
        //todo hash value comparison needs to be changed based on consistent hashing
        int hash = key.hashCode();

        List<MetaContent> l = kvStoreMetaData.getKvServerList();

        if (l.size() == 1) return l.get(0);

        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).getFromHash() <= hash
                    && hash < l.get(i + 1 % l.size()).getFromHash())
                return l.get(i);
        }

        throw new KVServerNotFoundException();
    }
}
