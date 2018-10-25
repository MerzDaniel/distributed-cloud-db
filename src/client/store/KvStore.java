package client.store;

import lib.KVMessage;

public class KvStore {
    final String host;
    final int port;
    public KvStore(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {

    }
    public void disconnect() {

    }
    public KVMessage get(String key) {return null;}
    public KVMessage put(String key, String value) {return null;}
}
