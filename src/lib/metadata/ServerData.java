package lib.metadata;

import lib.message.MarshallingException;

import java.math.BigInteger;
import java.util.Random;

public class ServerData {
    private final static String ELEMENT_SEPARATOR = "\u001F";

    String name;
    String host;
    int port;

    BigInteger fromHash = BigInteger.ZERO;

    public ServerData(String name, String host, int port, BigInteger fromHash) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.fromHash = fromHash;
    }

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
        if (this.host.equals(serverData.host) && port == serverData.port && this.fromHash.equals(serverData.fromHash)) {
            return true;
        }

        return false;
    }

    public BigInteger getFromHash() {
        return fromHash;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String marshall() {
        return name + ELEMENT_SEPARATOR + host + ELEMENT_SEPARATOR + port + ELEMENT_SEPARATOR + fromHash;
    }

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
