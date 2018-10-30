
import client.InteractionTest;
import integration.ConnectionTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import server.kv.CacheType;
import server.Server;

public class AllTests {

    static {
        try {
            Server s = new Server(50000, 10, CacheType.FIFO);
            new Thread(s).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        TestSuite clientSuite = new TestSuite("Basic Storage ServerTest-Suite");
        clientSuite.addTestSuite(ConnectionTest.class);
        clientSuite.addTestSuite(InteractionTest.class);
//        clientSuite.addTestSuite(AdditionalTest.class);
        return clientSuite;
    }

}
