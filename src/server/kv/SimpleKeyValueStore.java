package server.kv;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * This class is an implementaiton of a KeyValueStore with a {@link Reader} and a {@link Writer}
 * for reading and writing from the file
 *
 */
public class SimpleKeyValueStore implements KeyValueStore {

    final Logger logger = LogManager.getLogger(SimpleKeyValueStore.class);

    final File DB_DIRECTORY = new File(Paths.get("db").toUri());
    final File DB_FILE;
    final File TEMP_DB_FILE = new File(Paths.get(DB_DIRECTORY.toString(), "temp_db").toUri());

    Reader reader;
    Writer writer;
    private final String RECORD_SEPARATOR = "\u001E";

    public SimpleKeyValueStore(String dataFileName) {
        DB_FILE = new File(Paths.get(DB_DIRECTORY.toString(), "db_" + dataFileName).toUri());
    }

    public SimpleKeyValueStore(Reader reader, Writer writer, String dataFileName) {
        this.reader = reader;
        this.writer = writer;
        DB_FILE = new File(Paths.get(DB_DIRECTORY.toString(), "db_" + dataFileName).toUri());
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

    /**
     * shut downs the {@link KeyValueStore} by closing instances of any {@link java.io.Writer} or {@link java.io.Reader}
     *
     * @throws IOException if an error occured during the close of any {@link java.io.Writer} or {@link java.io.Reader}
     */
    @Override
    public void shutdown() throws IOException {
        reader.close();
        writer.close();
    }

    /**
     * Returns the value associated with the {@code key} in the database
     *
     * @param key for which the value is required
     * @return the value associated with the {@code key}
     * @throws KeyNotFoundException if the {@code key} is not found in the database
     * @throws DbError if any other errors happened during the retrival of associated value from the database
     */
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
            String[] split = line.split(RECORD_SEPARATOR);
            if (split.length != 2) continue;
            if (split[0].equals(key)) {
                return split[1];
            }
            rows++;
        }

        logger.debug(String.format("Parsed %drows without finding the key %s", rows, key));
        throw new KeyNotFoundException();
    }

    /**
     * Puts the specified {@code key} and {@code value} in the database
     *
     * @param key the key to be put in the database
     * @param value the value to be put in the database
     * @throws DbError if any errors happened while writing the {@code key} and {@code value} to the database
     */
    @Override
    public synchronized boolean put(String key, String value) throws DbError {
        try {
            ioDelete(key);
        } catch (KeyNotFoundException e) {
            logger.debug(String.format("Key %s is not present in the database", key));
        } catch (IOException e) {
            logger.error(String.format("An error occurred trying delete any existing keys", e.getLocalizedMessage()));
            throw new DbError(e);
        }
        //when the value is null or empty, just delete any existing record and return
        try {
            ioPut(key, value);
        } catch (IOException e) {
            logger.error("IO Exception during PUT", e);
            throw new DbError(e);
        }
        return false;
    }

    private void ioPut(String key, String value) throws IOException {
        writer.write(key + RECORD_SEPARATOR + value + System.getProperty("line.separator"));
        writer.flush();
    }

    /**
     * Returns whether the specified {@code key} is present in the database
     *
     * @param key the key
     * @return whether the key is present or not
     * @throws DbError if any any errors happened while checking the key is existing
     */
    @Override
    public boolean hasKey(String key) throws DbError {
        try {
            get(key);
        } catch (KeyNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * Deletes the entry with specified {@code key} from the database
     *
     * @param key the key which the entry is deleted
     * @return whether the delete of the entry was successful or not
     * @throws DbError if any errors happened while deleting the record from the database
     */
    @Override
    public synchronized boolean deleteKey(String key) throws DbError {
        try {
            ioDelete(key);
        } catch (IOException | KeyNotFoundException e) {
            logger.error("IO Exception during DELETE", e);
            throw new DbError(e);
        }
        return true;
    }

    @Override
    public Stream<AbstractMap.SimpleEntry<String, String>> retrieveAllData() {
        throw new NotImplementedException();
    }

    private String ioDelete(String key) throws IOException, KeyNotFoundException {
        Reader localReader = new FileReader(DB_FILE);
        Writer localWriter = new FileWriter(TEMP_DB_FILE);

        if (localReader.markSupported()) {
            logger.debug("Reset marker in FileReader.");
            localReader.reset();
        } else {
            logger.warn("Reset not supported!");
        }

        int rows = 0;
        boolean keyFound = false;
        try (final BufferedReader bufferedReader = new BufferedReader(localReader); final BufferedWriter bufferedWriter = new BufferedWriter(localWriter)) {
            for (Iterator<String> it = bufferedReader.lines().iterator(); it.hasNext(); ) {
                String line = it.next();
                String[] split = line.split(RECORD_SEPARATOR);
                if (split.length != 2) continue;
                if (!split[0].equals(key)) {
                    bufferedWriter.write(line + System.getProperty("line.separator"));
                } else {
                    keyFound = true;
                }
                rows++;
            }
        } catch (IOException e) {
            throw e;
        }

        boolean renameSuccess = TEMP_DB_FILE .renameTo(DB_FILE);
        if (!renameSuccess) {
            throw new IOException("An error occurred while renaming the database");
        }
        writer.close();
        writer = new FileWriter(DB_FILE, true);

        if (keyFound) {
            return key;
        }

        logger.debug(String.format("Parsed %drows without finding the key %s", rows, key));
        throw new KeyNotFoundException();
    }

}
