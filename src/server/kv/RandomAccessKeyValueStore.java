package server.kv;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.Iterator;

public class RandomAccessKeyValueStore implements KeyValueStore {

    final Logger logger = LogManager.getLogger(RandomAccessKeyValueStore.class);
    final File DB_DIRECTORY = new File(Paths.get("db").toUri());

    File DB_FILE;
    RandomAccessFile db;

    public RandomAccessKeyValueStore() {
        DB_FILE = new File(Paths.get(DB_DIRECTORY.toString(), "db").toUri());
    }

    @Override
    public void init() throws IOException {
        DB_DIRECTORY.mkdirs();
        try {
            db = new RandomAccessFile(DB_FILE, "rw");
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
    public void put(String key, String value) throws DbError {
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
            ioPut(key, value);
        } catch (IOException e) {
            logger.error("IO Exception during PUT", e);
            throw new DbError(e);
        }
    }

    private void ioPut(String key, String value) throws IOException {
        String newLine = key + "=" + value + System.lineSeparator();
        new FileWriter(DB_FILE, true).append(newLine).flush();
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
//        try {
//            ioDelete(key);
//        } catch (IOException | KeyNotFoundException e) {
//            logger.error("IO Exception during DELETE", e);
//            throw new DbError(e);
//        }
//        return true;
    }

    private boolean ioDelete(String key) throws IOException, KeyNotFoundException {
        return false;
//        Reader localReader = new FileReader(DB_FILE);
//        Writer localWriter = new FileWriter(TEMP_DB_FILE);
//
//        if (localReader.markSupported()) {
//            logger.debug("Reset marker in FileReader.");
//            localReader.reset();
//        } else {
//            logger.warn("Reset not supported!");
//        }
//
//        int rows = 0;
//        boolean keyFound = false;
//        try (final BufferedReader bufferedReader = new BufferedReader(localReader); final BufferedWriter bufferedWriter = new BufferedWriter(localWriter)) {
//            for (Iterator<String> it = bufferedReader.lines().iterator(); it.hasNext(); ) {
//                String line = it.next();
//                String[] split = line.split("=");
//                if (split.length != 2) continue;
//                if (!split[0].equals(key)) {
//                    bufferedWriter.write(line + System.getProperty("line.separator"));
//                } else {
//                    keyFound = true;
//                }
//                rows++;
//            }
//        } catch (IOException e) {
//            throw e;
//        }
//
//        boolean renameSuccess = TEMP_DB_FILE.renameTo(DB_FILE);
//        if (!renameSuccess) {
//            throw new IOException("An error occurred while renaming the database");
//        }
//        writer.close();
//        writer = new FileWriter(DB_FILE, true);
//
//        if (keyFound) {
//            return key;
//        }
//        logger.debug(String.format("Parsed %drows without finding the key %s", rows, key));
//        throw new KeyNotFoundException();
    }

}
