package server.kv;

import lib.StreamUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * This class is an implementaiton of a KeyValueStore with a {@link RandomAccessFile} thus having the
 * capability of reading and writing exact positions of the file without have to read all lines
 * <p>
 * Compared to {@link SimpleKeyValueStore} this has a huge performance gain
 */
public class RandomAccessKeyValueStore implements KeyValueStore {

    final Logger logger = LogManager.getLogger(RandomAccessKeyValueStore.class);
    final File DB_DIRECTORY = new File(Paths.get("db").toUri());

    File DB_FILE;
    RandomAccessFile db;

    private final String RECORD_SEPARATOR = "\u001E";

    public RandomAccessKeyValueStore() {

    }

    public RandomAccessKeyValueStore(File dbFile) {
        DB_FILE = dbFile;
    }

    @Override
    public void init(String dbName) throws IOException {
        try {
            DB_FILE = new File(Paths.get(DB_DIRECTORY.toString(), "db_" + dbName).toUri());
            DB_FILE.getParentFile().mkdirs();
            db = new RandomAccessFile(DB_FILE, "rw");
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
        if (db != null)
            db.close();
    }

    /**
     * Returns the value associated with the {@code key} in the database
     *
     * @param key for which the value is required
     * @return the value associated with the {@code key}
     * @throws KeyNotFoundException if the {@code key} is not found in the database
     * @throws DbError              if any other errors happened during the retrival of associated value from the database
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
        // go back to start of file
        db.seek(0);

        int rows = 0;
        while (true) {
            String nextLine = db.readLine();
            rows++;
            if (nextLine == null) break;

            AbstractMap.SimpleEntry<String, String> entry = parseLine(nextLine);

            if (entry == null) continue;
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }

        logger.debug(String.format("Parsed %drows without finding the key %s", rows, key));
        throw new KeyNotFoundException();
    }

    /**
     * Puts the specified {@code key} and {@code value} in the database
     *
     * @param key   the key to be put in the database
     * @param value the value to be put in the database
     * @throws DbError if any errors happened while writing the {@code key} and {@code value} to the database
     */
    @Override
    public synchronized boolean put(String key, String value) throws DbError {
        boolean deleted;
        try {
            deleted = ioDelete(key);
        } catch (IOException e) {
            logger.error(String.format("An error occurred trying delete any existing keys", e.getLocalizedMessage()));
            throw new DbError(e);
        }
        try {
            ioPut(key, value);
        } catch (IOException e) {
            logger.error("IO Exception during PUT", e);
            throw new DbError(e);
        }
        return deleted;
    }

    private boolean ioPut(String key, String value) throws IOException {
        String newLine = key + RECORD_SEPARATOR + value + System.lineSeparator();

        try (FileWriter writer = new FileWriter(DB_FILE, true)) {
            writer.append(newLine).flush();
        }
        return false;
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
            return true;
        } catch (IOException e) {
            logger.error("IO Exception during DELETE", e);
            throw new DbError(e);
        }
    }

    @Override
    public Stream<AbstractMap.SimpleEntry<String, String>> retrieveAllData() {
        Iterator<AbstractMap.SimpleEntry<String, String>> iterator = new Iterator<AbstractMap.SimpleEntry<String, String>>() {
            BufferedReader reader;
            AbstractMap.SimpleEntry<String, String> next = null;

            @Override
            public boolean hasNext() {
                if (next != null) return true;

                if (reader == null) {
                    try {
                        reader = new BufferedReader(new FileReader(DB_FILE));
                    } catch (FileNotFoundException e) {
                        logger.warn("File not found.", e);
                        return false;
                    }
                }

                try {
                    while (true) {
                        String nextLine = reader.readLine();
                        if (nextLine == null) break; // end of file
                        next = parseLine(nextLine);
                        if (next != null) return true;
                    }
                } catch (IOException e) {
                    logger.warn("Error while reading file", e);
                }

                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("Error while closing stream", e);
                }

                return false;
            }

            @Override
            public AbstractMap.SimpleEntry<String, String> next() {
                AbstractMap.SimpleEntry<String, String> next = this.next;
                this.next = null;
                return next;
            }
        };

        return StreamUtils.asStream(iterator, false);
    }

    private boolean ioDelete(String key) throws IOException {
        try (RandomAccessFile db = new RandomAccessFile(DB_FILE, "rw")) {
            long linePosition = 0;
            String nextLine;
            while (true) {
                linePosition = db.getFilePointer();
                nextLine = db.readLine();
                if (nextLine == null) break;

                AbstractMap.SimpleEntry<String, String> entry = parseLine(nextLine);

                if (entry == null || !entry.getKey().equals(key)) continue;

                db.seek(linePosition);
                byte[] emptyLine = new byte[nextLine.length()];
                Arrays.fill(emptyLine, (byte) 0);
                db.write(emptyLine);
                return true;
            }
        }
        return false;
    }

    private AbstractMap.SimpleEntry<String, String> parseLine(String line) {
        String[] split = line.split(RECORD_SEPARATOR);
        return split.length == 2 ? new AbstractMap.SimpleEntry<>(split[0], split[1]) : null;
    }
}
