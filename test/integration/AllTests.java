package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import server.kv.CacheType;
import server.Server;
import server.kv.KeyValueStore;
import server.kv.RandomAccessKeyValueStore;

import java.io.File;
import java.nio.file.Paths;

public class AllTests {

    static {
        try {
            File dbFile = new File(Paths.get("tmp", "INTEGRATION_TEST_DB").toUri());
            if (dbFile.exists()) dbFile.delete();
            KeyValueStore db = new RandomAccessKeyValueStore(dbFile);
            Server s = new Server(50000, 10, CacheType.FIFO, db);
            new Thread(s).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        TestSuite clientSuite = new TestSuite("Basic Storage ServerTest-Suite");
        clientSuite.addTestSuite(Connections.class);
        clientSuite.addTestSuite(Interactions.class);
//        clientSuite.addTestSuite(AdditionalTest.class);
        return clientSuite;
    }

}
