package lib.metadata;

import lib.Constants;
import lib.message.MarshallingException;
import lib.server.CacheType;

import java.math.BigInteger;

/**
 * This class stores the details about {@link server.KVServer} instance
 */
public class ServerData {

    String name;
    String host;
    int port;

    CacheType cacheType = CacheType.NONE;
    int cacheSize = 10;

    BigInteger fromHash = BigInteger.ZERO;

    /**
     * Create a {@Link ServerData} instance
     *
     * @param name name of the server
     * @param host host address of the server
     * @param port port of the server
     * @param fromHash starting hash from which the server stores keys
     * @param  cacheType CacheType
     * @param cacheSize cache size
     */
    public ServerData(String name, String host, int port, BigInteger fromHash, CacheType cacheType, int cacheSize) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.fromHash = fromHash;
        this.cacheType = cacheType;
        this.cacheSize = cacheSize;
    }

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
        return name + Constants.ELEMENT_SEPARATOR + host + Constants.ELEMENT_SEPARATOR + port + Constants.ELEMENT_SEPARATOR + fromHash + Constants.ELEMENT_SEPARATOR + cacheType.name() + Constants.ELEMENT_SEPARATOR + cacheSize;
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
            String[] split = kvServerMetaData.split(Constants.ELEMENT_SEPARATOR);
            int port = Integer.parseInt(split[2]);
            BigInteger hash = new BigInteger(split[3]);
            CacheType cacheType = CacheType.valueOf(split[4]);
            int cacheSize = Integer.parseInt(split[5]);

            return new ServerData(split[0], split[1], port, hash, cacheType, cacheSize);
        } catch (Exception ex) {
            throw new MarshallingException(ex);
        }
    }
}
