package integration;

import client.store.KvStore;
import junit.framework.TestCase;

import java.net.UnknownHostException;


public class Connections extends TestCase {


    public void testConnectionSuccess() {

        Exception ex = null;

        KvStore kvClient = new KvStore("localhost", 50000);
        try {
            kvClient.connect();
        } catch (Exception e) {
            ex = e;
        }

        assertNull(ex);
    }


    public void testUnknownHost() {
        Exception ex = null;
        KvStore kvClient = new KvStore("unknown", 50000);

        try {
            kvClient.connect();
        } catch (Exception e) {
            ex = e;
        }

        assertTrue(ex instanceof UnknownHostException);
    }


    public void testIllegalPort() {
        Exception ex = null;
        KvStore kvClient = new KvStore("localhost", 123456789);

        try {
            kvClient.connect();
        } catch (Exception e) {
            ex = e;
        }

        assertTrue(ex instanceof IllegalArgumentException);
    }

}

