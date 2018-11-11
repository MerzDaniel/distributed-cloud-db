package lib.server.metadata;

import lib.message.UnmarshallException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KVStoreMetaData {
    private final static String RECORD_SEPARATOR = "\u001E";

    private List<MetaContent> kvServerList = new ArrayList<>();

    public KVStoreMetaData(List<MetaContent> kvServerList) {
        this.kvServerList = kvServerList;
    }

    public List<MetaContent> getKvServerList() {
        return kvServerList;
    }

    public static String marshall(KVStoreMetaData kvStoreMetaData) {
        return kvStoreMetaData.getKvServerList().stream().map(it -> MetaContent.marshall(it)).collect(Collectors.joining(RECORD_SEPARATOR));
    }

    public static KVStoreMetaData unmarshall(String kvStoreMetaData) throws UnmarshallException {
        List<MetaContent> metaContentList = new ArrayList<>();
        String[] kvServers = kvStoreMetaData.split(RECORD_SEPARATOR);

        for (String kvServer : kvServers) {
            MetaContent metaContent = MetaContent.unmarshall(kvServer);
            metaContentList.add(metaContent);
        }

        return new KVStoreMetaData(metaContentList);
    }

    public static class MetaContent {
        private final static String ELEMENT_SEPARATOR = "\u001F";

        String host;
        String port;
        int fromHash;
        int toHash;

        public MetaContent(String host, String port, int fromHash, int toHash) {
            this.host = host;
            this.port = port;
            this.fromHash = fromHash;
            this.toHash = toHash;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MetaContent)) {
                return false;
            }

            MetaContent metaContent = (MetaContent) o;
            if (this.host.equals(metaContent.host) && this.port.equals(metaContent.port) && this.fromHash == metaContent.fromHash && this.toHash == metaContent.toHash) {
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

        public static String marshall(MetaContent metaContent) {
            return metaContent.host + ELEMENT_SEPARATOR + metaContent.port + ELEMENT_SEPARATOR + metaContent.fromHash + ELEMENT_SEPARATOR + metaContent.toHash;
        }

        public static MetaContent unmarshall(String kvServerMetaData) throws UnmarshallException {

            try {
                String[] split = kvServerMetaData.split(ELEMENT_SEPARATOR);
                return new MetaContent(split[0], split[1], Integer.valueOf(split[2]), Integer.valueOf(split[3]));
            } catch (Exception ex) {
                throw new UnmarshallException(ex);
            }
        }
    }
}
