package server.kv;

public class KeyNotFoundException extends Exception {
    public KeyNotFoundException() {

    }
    public KeyNotFoundException(String key) {
        super("The key " + key + " was not found");
    }
}
