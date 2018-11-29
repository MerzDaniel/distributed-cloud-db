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
 */
public class RandomAccessKeyValueStore implements KeyValueStore {

    final Logger logger = LogManager.getLogger(RandomAccessKeyValueStore.class);
    final File DB_DIRECTORY = new File(Paths.get("db").toUri());

    public File DB_FILE;
    public File INDEX_FILE;
    RandomAccessFile db;
    DbIndex index;

    private final String RECORD_SEPARATOR = "\u001E";

    public RandomAccessKeyValueStore() {
    }

    @Override
    public void init(String dbName) throws IOException {
        try {
            DB_FILE = new File(Paths.get(DB_DIRECTORY.toString(), "db_" + dbName).toUri());
            DB_FILE.getParentFile().mkdirs();
            INDEX_FILE = new File(Paths.get(DB_DIRECTORY.toString(), "index_" + dbName).toUri());
            index = DbIndex.LoadFromFile(INDEX_FILE);
            db = new RandomAccessFile(DB_FILE, "rw");
        } catch (IOException e) {
            logger.error("Error while initializing database", e);
            throw e;
        } catch (ClassNotFoundException e) {
            logger.error("Could not load index CLASSNOTFOUND", e);
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
        index.save(INDEX_FILE);
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
        try (RandomAccessFile db = new RandomAccessFile(DB_FILE, "r")) {
            DbIndex.IndexEntry indexEntry = index.getEntry(key);
            if (indexEntry == null) throw new KeyNotFoundException();

            db.seek(indexEntry.offset);
            String line = db.readLine();
            AbstractMap.SimpleEntry<String, String> entry = parseLine(line);
            // todo: Fix problem for key that is deleted while reading it
            if (entry == null) throw new KeyNotFoundException();
            return entry.getValue();
        }
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
        // todo think about parallel puts!!
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


        int offset = (int) DB_FILE.length();
        int length = newLine.length();
        index.putKey(key, new DbIndex.IndexEntry(offset, length));
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
        return index.hasKey(key);
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
            DbIndex.IndexEntry indexEntry = index.getEntry(key);
            db.seek(indexEntry.offset);
            byte[] emptyLine = new byte[indexEntry.length];
            Arrays.fill(emptyLine, (byte) 0);
            db.write(emptyLine);
            return true;
        }
    }

    private AbstractMap.SimpleEntry<String, String> parseLine(String line) {
        String[] split = line.split(RECORD_SEPARATOR);
        return split.length == 2 ? new AbstractMap.SimpleEntry<>(split[0], split[1]) : null;
    }
}
