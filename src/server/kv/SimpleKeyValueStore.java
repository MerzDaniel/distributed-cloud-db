package server.kv;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.Iterator;

public class SimpleKeyValueStore implements KeyValueStore {

    final Logger logger = LogManager.getLogger(SimpleKeyValueStore.class);

    final static File DB_DIRECTORY = new File(Paths.get(".", "db").toUri());
    final static File DB_FILE = new File(Paths.get(DB_DIRECTORY.toString(), "db").toUri());

    final Reader reader;
    final Writer writer;

    public SimpleKeyValueStore() throws IOException {
        DB_DIRECTORY.mkdirs();
        if (!DB_FILE.createNewFile()) {
        }
        reader = new FileReader(DB_FILE);
        writer = new FileWriter(DB_FILE);
    }

    public SimpleKeyValueStore(Reader reader, Writer writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public String get(String key) throws DbError, KeyNotFoundException {
        try {
            return ioGet(key);
        } catch (IOException e) {
            logger.error("Error while resetting buffer!", e);
            throw new DbError(e);
        }
    }

    private String ioGet(String key) throws IOException, KeyNotFoundException {
        if (reader.markSupported()) reader.reset();
        final BufferedReader bufferedReader = new BufferedReader(reader);
        for (Iterator<String> it = bufferedReader.lines().iterator(); it.hasNext(); ) {
            String line = it.next();
            String[] split = line.split("=");
            if (split.length != 2) continue;
            if (split[0].equals(key)) {
                return split[1];
            }
        }

        throw new KeyNotFoundException();
    }

    @Override
    public void put(String key, String value) throws DbError {
        try {
            ioPut(key, value);
        } catch (IOException e) {
            throw new DbError(e);
        }
    }
    private void ioPut(String key, String value) throws IOException {
        writer.append(key + "=" + value + "\n");
    }

    @Override
    public boolean hasKey(String key) throws DbError {
        try {
            get(key);
        } catch (KeyNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteKey() throws DbError {


        return false;
    }

}
