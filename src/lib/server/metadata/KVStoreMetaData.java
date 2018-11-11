package lib.server.metadata;

import lib.message.UnmarshallException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KVStoreMetaData {
    private final static String RECORD_SEPARATOR = "\u001E";

    private List<KVServerMetaData> kvServerList = new ArrayList<>();

    public KVStoreMetaData(List<KVServerMetaData> kvServerList) {
        this.kvServerList = kvServerList;
    }

    public List<KVServerMetaData> getKvServerList() {
        return kvServerList;
    }

    public static String marshallKvStoreMetaData(KVStoreMetaData kvStoreMetaData) {
        return kvStoreMetaData.getKvServerList().stream().map(it -> KVServerMetaData.marshallKvServerMetaData(it)).collect(Collectors.joining(RECORD_SEPARATOR));
    }

    public static KVStoreMetaData unmarshallKVStoreMetaData(String kvStoreMetaData) throws UnmarshallException {
        List<KVStoreMetaData.KVServerMetaData> kvServerMetaDataList = new ArrayList<>();
        String[] kvServers = kvStoreMetaData.split(RECORD_SEPARATOR);

        for (String kvServer : kvServers) {
            KVStoreMetaData.KVServerMetaData kvServerMetaData = KVServerMetaData.unmarshallKvServerMetaData(kvServer);
            kvServerMetaDataList.add(kvServerMetaData);
        }

        return new KVStoreMetaData(kvServerMetaDataList);
    }

    public static class KVServerMetaData {
        private final static String ELEMENT_SEPARATOR = "\u001F";

        String host;
        String port;
        int fromHash;
        int toHash;

        public KVServerMetaData(String host, String port, int fromHash, int toHash) {
            this.host = host;
            this.port = port;
            this.fromHash = fromHash;
            this.toHash = toHash;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof KVServerMetaData)) {
                return false;
            }

            KVServerMetaData kvServerMetaData = (KVServerMetaData) o;
            if (this.host.equals(kvServerMetaData.host) && this.port.equals(kvServerMetaData.port) && this.fromHash == kvServerMetaData.fromHash && this.toHash == kvServerMetaData.toHash) {
                return true;
            }

            return false;
        }

        public int getFromHash() {
            return fromHash;
        }

        public String getHost() {
            return host;
        }

        public String getPort() {
            return port;
        }

        public int getToHash() {
            return toHash;
        }

        public static String marshallKvServerMetaData(KVStoreMetaData.KVServerMetaData kvServerMetaData) {
            return kvServerMetaData.host + ELEMENT_SEPARATOR + kvServerMetaData.port + ELEMENT_SEPARATOR + kvServerMetaData.fromHash + ELEMENT_SEPARATOR + kvServerMetaData.toHash;
        }

        public static KVStoreMetaData.KVServerMetaData unmarshallKvServerMetaData(String kvServerMetaData) throws UnmarshallException {

            try {
                String[] split = kvServerMetaData.split(ELEMENT_SEPARATOR);
                return new KVStoreMetaData.KVServerMetaData(split[0], split[1], Integer.valueOf(split[2]), Integer.valueOf(split[3]));
            } catch (Exception ex) {
                throw new UnmarshallException(ex);
            }
        }
    }
}
