package integration;

import junit.framework.Test;
import junit.framework.TestSuite;
import lib.server.RunningState;
import lib.server.CacheType;
import server.KVServer;
import server.kv.KeyValueStore;
import server.kv.RandomAccessKeyValueStore;

import java.io.File;
import java.nio.file.Paths;

public class AllTests {

    static {
        System.setProperty("log4j.configurationFile", "log4j2-test.properties.xml");
        try {
            KeyValueStore db = new RandomAccessKeyValueStore();
            db.init("dbname");
            if (((RandomAccessKeyValueStore) db).DB_FILE.exists()) ((RandomAccessKeyValueStore) db).DB_FILE.delete();

            KVServer s = new KVServer("server", "localhost", 50000, 10, CacheType.FIFO, db, RunningState.RUNNING);
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
