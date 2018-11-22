package tools.util;

import client.store.KVStore;
import lib.message.KVMessage;
import lib.message.MarshallingException;
import lib.metadata.KVServerNotFoundException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Command {
    private final String key;
    private final String value;
    private String command;

    public Command(String key, String value, String command) {

        this.key = key;
        this.value = value;
        this.command = command;
    }
    public KVMessage run(KVStore kv) throws KVServerNotFoundException, NoSuchAlgorithmException, MarshallingException, IOException {
        if (command.equals("GET"))
            return kv.get(key);
        else if (command.equals("PUT"))
            return kv.put(key, value);
        return null;
    }
}
