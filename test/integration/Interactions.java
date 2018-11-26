package integration;

import client.store.KVStore;
import junit.framework.TestCase;
import lib.message.KVMessage;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class Interactions extends TestCase {

    private KVStore kvClient;

    @Before
    public void setUp() throws IOException {
        kvClient = new KVStore(new KVStoreMetaData(Arrays.asList(new ServerData("server", "localhost", 50000))));
//        kvClient.connect("localhost", 50000);
    }

    @After
    public void tearDown() {
        kvClient.disconnect();
    }


    @Test
    public void Put() {
        String key = "foofoofoo";
        String value = "bar";
        KVMessage response = null;
        Exception ex = null;

        try {
            response = kvClient.put(key, value);
        } catch (Exception e) {
            ex = e;
        }

        assertTrue(
                String.format("ERROR: %s, response: %s",
                        ex != null ? ex.getMessage() : "",
                        response!=null?response.getStatus().toString():""),
                ex == null && response.getStatus() == KVMessage.StatusType.PUT_SUCCESS);
    }

    @Test
    public void PutDisconnected() {
        kvClient.disconnect();
        String key = "asdlfkjasdf";
        String value = "bar";
        Exception ex = null;

        KVMessage result = null;
        try {
            result = kvClient.put(key, value);
        } catch (Exception e) {
            ex = e;
            assertTrue(e.getMessage(), false);
        }

        assertEquals(KVMessage.StatusType.PUT_SUCCESS, result.getStatus());
    }

    @Test
    public void testUpdate() {
        String key = "updateTestValue";
        String initialValue = "initial";
        String updatedValue = "updated";

        KVMessage response = null;
        try {
            kvClient.put(key, initialValue);
            response = kvClient.put(key, updatedValue);

        } catch (Exception e) {
            assertTrue(false);
        }

        assertEquals(KVMessage.StatusType.PUT_UPDATE, response.getStatus());
    }

    @Test
    public void testDelete() {
        String key = "deleteTestValue";
        String value = "toDelete";

        KVMessage response = null;
        try {
            kvClient.put(key, value);
            response = kvClient.put(key, "null");
        } catch (Exception e) {
            assertTrue(false);
        }

        assertEquals(KVMessage.StatusType.DELETE_SUCCESS, response.getStatus());
    }

    @Test
    public void testGet() {
        String key = "foo";
        String value = "bar";
        KVMessage response = null;

        String result = "";
        try {
            kvClient.put(key, value);
            response = kvClient.get(key);
            result = response.getValue();
        } catch (Exception e) {
            assertTrue(e.getMessage(), false);
        }

        assertEquals(KVMessage.StatusType.GET_SUCCESS, response.getStatus());
        assertTrue(result.equals("bar"));
    }

    @Test
    public void testGetUnsetValue() {
        String key = "an unset value";
        KVMessage response = null;
        Exception ex = null;

        try {
            response = kvClient.get(key);
        } catch (Exception e) {
            ex = e;
        }

        assertEquals(KVMessage.StatusType.GET_NOT_FOUND, response.getStatus());
    }

}