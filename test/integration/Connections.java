package integration;

import client.store.KVStore;
import junit.framework.TestCase;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;

import java.net.UnknownHostException;
import java.util.Arrays;


public class Connections extends TestCase {


    public void testConnectionSuccess() {

        Exception ex = null;

        KVStore kvClient = new KVStore(new KVStoreMetaData(Arrays.asList(new ServerData("127.0.0.4", 30000))));
        try {
            kvClient.connect("localhost", 50000);
        } catch (Exception e) {
            ex = e;
        }

        assertNull(ex);
    }


    public void testUnknownHost() {
        Exception ex = null;
        KVStore kvClient = new KVStore(new KVStoreMetaData(Arrays.asList(new ServerData("localhost", 50000))));

        try {
            kvClient.connect("unknown", 50000);
        } catch (Exception e) {
            ex = e;
        }

        assertTrue(ex instanceof UnknownHostException);
    }


    public void testIllegalPort() {
        Exception ex = null;
        KVStore kvClient = new KVStore(new KVStoreMetaData(Arrays.asList(new ServerData("localhost", 50000))));

        try {
            kvClient.connect("localhost", 123456789);
        } catch (Exception e) {
            ex = e;
        }

        assertTrue(ex instanceof IllegalArgumentException);
    }

}

