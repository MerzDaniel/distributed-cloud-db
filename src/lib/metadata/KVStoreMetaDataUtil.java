package lib.metadata;

import lib.hash.HashUtil;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public class KVStoreMetaDataUtil {

    public static MetaContent getKVServer(KVStoreMetaData kvStoreMetaData, String key) throws KVServerNotFoundException, NoSuchAlgorithmException {
        BigInteger hash = HashUtil.getHash(key);

        List<MetaContent> l = kvStoreMetaData.getKvServerList();

        if (l.size() == 1) return l.get(0);

        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).getFromHash().compareTo(hash) == -1
                    && hash.compareTo(l.get(i + 1 % l.size()).getFromHash()) == -1)
                return l.get(i);
        }

        throw new KVServerNotFoundException();
    }
}
