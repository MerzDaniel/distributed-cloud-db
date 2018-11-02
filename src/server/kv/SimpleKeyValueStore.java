package server.kv;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.Iterator;

public class SimpleKeyValueStore implements KeyValueStore {

    final Logger logger = LogManager.getLogger(SimpleKeyValueStore.class);

    final static File DB_DIRECTORY = new File(Paths.get("db").toUri());
    final static File DB_FILE = new File(Paths.get(DB_DIRECTORY.toString(), "db").toUri());

    Reader reader;
    Writer writer;

    public SimpleKeyValueStore() {

    }

    public SimpleKeyValueStore(Reader reader, Writer writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void init() throws IOException {
        DB_DIRECTORY.mkdirs();
        try {
            if (!DB_FILE.exists()) {
                logger.info("Creating db file: " + DB_FILE.getAbsolutePath());
                DB_FILE.createNewFile();
            }
            writer = new FileWriter(DB_FILE, true);
        } catch (IOException e) {
            logger.error("Error while initializing database", e);
            throw e;
        }
    }

    @Override
    public String get(String key) throws DbError, KeyNotFoundException {
        try {
            return ioGet(key);
        } catch (IOException e) {
            logger.error("IO Exception during GET", e);
            throw new DbError(e);
        }
    }

    private String ioGet(String key) throws IOException, KeyNotFoundException {
        Reader localReader;
        if (reader != null) localReader = reader;
        else localReader = new FileReader(DB_FILE);

        if (localReader.markSupported()) {
            logger.debug("Reset marker in FileReader.");
            localReader.reset();
        } else {
            logger.warn("Reset not supported!");
        }
        final BufferedReader bufferedReader = new BufferedReader(localReader);
        int rows = 0;
        for (Iterator<String> it = bufferedReader.lines().iterator(); it.hasNext(); ) {
            String line = it.next();
            String[] split = line.split("=");
            if (split.length != 2) continue;
            if (split[0].equals(key)) {
                return split[1];
            }
            rows++;
        }

        logger.debug(String.format("Parsed %drows without finding the key %s", rows, key));
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
        writer.write(key + "=" + value + "\n");
        writer.flush();
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
    public boolean deleteKey(String key) throws DbError {
        return false;
    }

}
