package server.kv;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.Arrays;

public class RandomAccessKeyValueStore implements KeyValueStore {

    final Logger logger = LogManager.getLogger(RandomAccessKeyValueStore.class);
    final File DB_DIRECTORY = new File(Paths.get("db").toUri());

    File DB_FILE;
    RandomAccessFile db;

    public RandomAccessKeyValueStore() {
        DB_FILE = new File(Paths.get(DB_DIRECTORY.toString(), "db").toUri());
    }
    public RandomAccessKeyValueStore(File dbFile) {
        DB_FILE = dbFile;
    }

    @Override
    public void init() throws IOException {
        try {
            DB_FILE.getParentFile().mkdirs();
            db = new RandomAccessFile(DB_FILE, "rw");
        } catch (IOException e) {
            logger.error("Error while initializing database", e);
            throw e;
        }
    }

    @Override
    public void shutdown() throws IOException {
        db.close();
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
        // go back to start of file
        db.seek(0);

        int rows = 0;
        while(true) {
            String nextLine = db.readLine();
            rows++;
            if (nextLine == null) break;

            String[] split = nextLine.split("=");
            if (split.length != 2) continue;
            if (split[0].equals(key)) {
                return split[1];
            }
        }

        logger.debug(String.format("Parsed %drows without finding the key %s", rows, key));
        throw new KeyNotFoundException();
    }

    @Override
    public boolean put(String key, String value) throws DbError {
//        try {
//            ioDelete(key);
//        } catch (KeyNotFoundException e) {
//            logger.debug(String.format("Key %s is not present in the database", key));
//        } catch (IOException e) {
//            logger.error(String.format("An error occurred trying delete any existing keys", e.getLocalizedMessage()));
//            throw new DbError(e);
//        }
        //when the value is null or empty, just delete any existing record and return
//        if (value == "" || value == null) {
//            return;
//        }
        try {
            return ioPut(key, value);
        } catch (IOException e) {
            logger.error("IO Exception during PUT", e);
            throw new DbError(e);
        }
    }

    private boolean ioPut(String key, String value) throws IOException {
        String newLine = key + "=" + value + System.lineSeparator();

        try (FileWriter writer = new FileWriter(DB_FILE, true)) {
            writer.append(newLine).flush();
        }
        return false;
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
        try {
            ioDelete(key);
            return true;
        } catch (IOException | KeyNotFoundException e) {
            logger.error("IO Exception during DELETE", e);
            throw new DbError(e);
        }
    }

    private boolean ioDelete(String key) throws IOException, KeyNotFoundException {
        try (RandomAccessFile db = new RandomAccessFile(DB_FILE, "rw")) {
            long linePosition = 0;
            String nextLine;
            while (true) {
                linePosition = db.getFilePointer();
                nextLine = db.readLine();
                if (nextLine == null) break;

                String[] split = nextLine.split("=");
                if (split.length != 2) continue;
                if (split[0].equals(key)) {
                    db.seek(linePosition);
                    byte[] emptyLine = new byte[nextLine.length() * 2]; // chars are represented with two bytes
                    Arrays.fill(emptyLine, (byte) Character.getNumericValue(' '));
                    db.write(emptyLine);
                    return true;
                }
            }
        }
        return false;
    }

}
