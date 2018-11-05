package integration;

import client.store.KVStore;
import junit.framework.TestCase;
import lib.message.KVMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class Interactions extends TestCase {

    private KVStore kvClient;

    @Before
    public void setUp() throws IOException {
        kvClient = new KVStore("localhost", 50000);
        kvClient.connect();
    }

    @After
    public void tearDown() {
        kvClient.disconnect();
    }


    @Test
    public void testPut() {
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
    public void testPutDisconnected() {
        kvClient.disconnect();
        String key = "foo";
        String value = "bar";
        Exception ex = null;

        try {
            kvClient.put(key, value);
        } catch (Exception e) {
            ex = e;
        }

        assertNotNull(ex);
    }

    @Test
    public void testUpdate() {
        String key = "updateTestValue";
        String initialValue = "initial";
        String updatedValue = "updated";

        KVMessage response = null;
        Exception ex = null;

        try {
            kvClient.put(key, initialValue);
            response = kvClient.put(key, updatedValue);

        } catch (Exception e) {
            ex = e;
        }

        assertTrue(ex == null && response.getStatus() == KVMessage.StatusType.PUT_UPDATE);
    }

    @Test
    public void testDelete() {
        String key = "deleteTestValue";
        String value = "toDelete";

        KVMessage response = null;
        Exception ex = null;

        try {
            kvClient.put(key, value);
            response = kvClient.put(key, "");
        } catch (Exception e) {
            ex = e;
        }

        assertTrue(ex == null && response.getStatus() == KVMessage.StatusType.DELETE_SUCCESS);
    }

    @Test
    public void testGet() {
        String key = "foo";
        String value = "bar";
        KVMessage response = null;
        Exception ex = null;

        try {
            kvClient.put(key, value);
            response = kvClient.get(key);
        } catch (Exception e) {
            ex = e;
        }

        assertTrue(ex == null && response.getValue().equals("bar"));
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