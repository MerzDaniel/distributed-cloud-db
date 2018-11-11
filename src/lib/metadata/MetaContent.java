package lib.metadata;

import lib.message.MarshallingException;

public class MetaContent {
    private final static String ELEMENT_SEPARATOR = "\u001F";

    String host;
    int port;
    int fromHash;
    int toHash;

    public MetaContent(String host, int port, int fromHash, int toHash) {
        this.host = host;
        this.port = port;
        this.fromHash = fromHash;
        this.toHash = toHash;
    }

    public MetaContent(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MetaContent)) {
            return false;
        }

        MetaContent metaContent = (MetaContent) o;
        if (this.host.equals(metaContent.host) && port == metaContent.port && this.fromHash == metaContent.fromHash && this.toHash == metaContent.toHash) {
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

    public int getPort() {
        return port;
    }

    public int getToHash() {
        return toHash;
    }

    public static String marshall(MetaContent metaContent) {
        return metaContent.host + ELEMENT_SEPARATOR + metaContent.port + ELEMENT_SEPARATOR + metaContent.fromHash + ELEMENT_SEPARATOR + metaContent.toHash;
    }

    public static MetaContent unmarshall(String kvServerMetaData) throws MarshallingException {

        try {
            String[] split = kvServerMetaData.split(ELEMENT_SEPARATOR);
            int port = Integer.parseInt(split[1]);
            return new MetaContent(split[0], port, Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        } catch (Exception ex) {
            throw new MarshallingException(ex);
        }
    }
}
