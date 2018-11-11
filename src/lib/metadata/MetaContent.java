package lib.metadata;

import lib.message.MarshallingException;

public class MetaContent {
    private final static String ELEMENT_SEPARATOR = "\u001F";

    String host;
    int port;
    int fromHash;

    public MetaContent(String host, int port, int fromHash) {
        this.host = host;
        this.port = port;
        this.fromHash = fromHash;
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
        if (this.host.equals(metaContent.host) && port == metaContent.port && this.fromHash == metaContent.fromHash) {
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

    public String marshall() {
        return host + ELEMENT_SEPARATOR + port + ELEMENT_SEPARATOR + fromHash;
    }

    public static MetaContent unmarshall(String kvServerMetaData) throws MarshallingException {

        try {
            String[] split = kvServerMetaData.split(ELEMENT_SEPARATOR);
            int port = Integer.parseInt(split[1]);
            return new MetaContent(split[0], port, Integer.parseInt(split[2]));
        } catch (Exception ex) {
            throw new MarshallingException(ex);
        }
    }
}
