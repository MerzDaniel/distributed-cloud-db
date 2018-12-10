package server.kv;

import java.io.*;
import java.util.Dictionary;
import java.util.Hashtable;

public class DbIndex {

    private DbIndex(Dictionary<String, IndexEntry> index) {
        this.index = index;
    }

    private Dictionary<String, IndexEntry> index;

    public IndexEntry getEntry(String key) {
        return index.get(key);
    }

    public void putKey(String key, IndexEntry entry) {
        index.put(key, entry);
    }

    public boolean hasKey(String key) {
        return index.get(key) != null;
    }

    public void save(File indexFile) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFile));
        oos.writeObject(index);
    }

    public static DbIndex LoadFromFile(File indexFile) throws IOException, ClassNotFoundException {
        if (!indexFile.exists()) {
            return new DbIndex(new Hashtable<>());
        }
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile));
        return new DbIndex((Dictionary<String, IndexEntry>) ois.readObject());
    }

    public void remove(String key) {
        index.remove(key);
    }

    public static class IndexEntry implements Serializable {
        public int offset;
        public int length;

        public IndexEntry(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
    }
}
