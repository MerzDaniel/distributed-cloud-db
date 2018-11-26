package lib.metadata;

import lib.message.MarshallingException;
import lib.server.CacheType;

import java.math.BigInteger;

/**
 * This class stores the details about {@link server.KVServer} instance
 */
public class ServerData {
    private final static String ELEMENT_SEPARATOR = "\u001F";

    String name;
    String host;
    int port;


    CacheType cacheType;
    int cacheSize;

    BigInteger fromHash = BigInteger.ZERO;

    /**
     * Create a {@Link ServerData} instance
     *
     * @param name name of the server
     * @param host host address of the server
     * @param port port of the server
     * @param fromHash starting hash from which the server stores keys
     */
    public ServerData(String name, String host, int port, BigInteger fromHash) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.fromHash = fromHash;
    }

    /**
     * Create a {@Link ServerData} instance
     *
     * @param name name of the server
     * @param host host address of the server
     * @param port port of the server
     */
    public ServerData(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServerData)) {
            return false;
        }

        ServerData serverData = (ServerData) o;
        return this.host.equals(serverData.host) && this.port == serverData.port;// && this.fromHash.equals(serverData.fromHash);
    }

    public BigInteger getFromHash() {
        return fromHash;
    }

    public String getName() { return name; }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
    /**
     * Marshall this {@link ServerData} instance
     *
     * @return marshalled string
     */
    public String marshall() {
        return name + ELEMENT_SEPARATOR + host + ELEMENT_SEPARATOR + port + ELEMENT_SEPARATOR + fromHash;
    }

    @Override
    public String toString() {
        return String.format("%s %s:%d", name, host, port);
    }

    /**
     * UNmarshall the {@code kvServerMetaData} string
     *
     * @param kvServerMetaData string to be unmarshalled
     * @return {@link ServerData} instance
     * @throws MarshallingException if any exception occurs during unmarshalling
     */
    public static ServerData unmarshall(String kvServerMetaData) throws MarshallingException {

        try {
            String[] split = kvServerMetaData.split(ELEMENT_SEPARATOR);
            int port = Integer.parseInt(split[2]);
            BigInteger hash = new BigInteger(split[3]);
            return new ServerData(split[0], split[1], port, hash);
        } catch (Exception ex) {
            throw new MarshallingException(ex);
        }
    }
}
