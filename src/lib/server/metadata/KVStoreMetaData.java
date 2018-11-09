package lib.server.metadata;

import java.util.ArrayList;
import java.util.List;

public class KVStoreMetaData {

    public static class KVServerMetaData {
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
    }

    private List<KVServerMetaData> kvServerList = new ArrayList<>();

    public KVStoreMetaData(List<KVServerMetaData> kvServerList) {
        this.kvServerList = kvServerList;
    }

    public List<KVServerMetaData> getKvServerList() {
        return kvServerList;
    }
}
