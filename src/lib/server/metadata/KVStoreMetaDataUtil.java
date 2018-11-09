package lib.server.metadata;

import lib.message.UnmarshallException;
import lib.server.KVServerNotFoundException;
import lib.server.metadata.KVStoreMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public static String marshallKvStoreMetaData(KVStoreMetaData kvStoreMetaData) {
        final String RECORD_SEPARATOR = "\u001E";
        return kvStoreMetaData.getKvServerList().stream().map(it -> KVStoreMetaDataUtil.marshallKvServerMetaData(it)).collect(Collectors.joining(RECORD_SEPARATOR));
    }

    private static String marshallKvServerMetaData(KVStoreMetaData.KVServerMetaData kvServerMetaData) {
        final String ELEMENT_SEPARATOR = "\u001F";
        return kvServerMetaData.host + ELEMENT_SEPARATOR + kvServerMetaData.port + ELEMENT_SEPARATOR + kvServerMetaData.fromHash + ELEMENT_SEPARATOR + kvServerMetaData.toHash;
    }

    public static KVStoreMetaData unmarshallKVStoreMetaData(String kvStoreMetaData) throws UnmarshallException {
        final String RECORD_SEPARATOR = "\u001E";

        List<KVStoreMetaData.KVServerMetaData> kvServerMetaDataList = new ArrayList<>();
        String[] kvServers = kvStoreMetaData.split(RECORD_SEPARATOR);

        for (String kvServer : kvServers) {
            KVStoreMetaData.KVServerMetaData kvServerMetaData = KVStoreMetaDataUtil.unmarshallKvServerMetaData(kvServer);
            kvServerMetaDataList.add(kvServerMetaData);
        }

        return new KVStoreMetaData(kvServerMetaDataList);
    }

    private static KVStoreMetaData.KVServerMetaData unmarshallKvServerMetaData(String kvServerMetaData) throws UnmarshallException {
        final String ELEMENT_SEPARATOR = "\u001F";

        try {
            String[] split = kvServerMetaData.split(ELEMENT_SEPARATOR);
            return new KVStoreMetaData.KVServerMetaData(split[0], split[1], Integer.valueOf(split[2]), Integer.valueOf(split[3]));
        } catch (Exception ex) {
            throw new UnmarshallException(ex);
        }
    }
}
