
import client.InteractionTest;
import integration.ConnectionTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import server.CacheType;
import server.Server;

public class AllTests {

    static {
        try {
            new Server(50000, 10, CacheType.FIFO);
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