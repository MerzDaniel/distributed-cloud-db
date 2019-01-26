package lib;

import lib.json.Json;
import lib.message.exception.MarshallingException;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class JsonTest {

    @Test
    public void testSerialize() throws MarshallingException {
        Json j = Json.Builder.create().withStringProperty("name", "Jhon").finish();
        String s = j.serialize();
        assertEquals("{\"name\":\"Jhon\"}", s);
        assertEquals(s, Json.deserialize(s).serialize());
    }

    @Test
    public void testSerializeMultipleProperties() throws MarshallingException {
        Json j = Json.Builder.create()
                .withStringProperty("name", "Jhon")
                .withStringProperty("age", "35")
                .withStringProperty("country", "USA")
                .finish();
        String s = j.serialize();
        assertEquals("{\"name\":\"Jhon\",\"age\":\"35\",\"country\":\"USA\"}", s);
        assertEquals(s, Json.deserialize(s).serialize());
    }

    @Test
    public void testSerializeComplexProperties() throws MarshallingException {

        Json f = Json.Builder.create()
                .withStringProperty("name", "Khan")
                .withStringProperty("age", "34")
                .finish();

        Json j = Json.Builder.create()
                .withStringProperty("name", "Jhon")
                .withStringProperty("age", "35")
                .withStringProperty("country", "USA")
                .withJsonProperty("friend", f)
                .finish();
        
        String s = j.serialize();
        assertEquals("{\"name\":\"Jhon\",\"age\":\"35\",\"country\":\"USA\",\"friend\":{\"name\":\"Khan\",\"age\":\"34\"}}", s);

        assertEquals(s, Json.deserialize(s).serialize());
    }

    @Test
    public void testSerializeUndefined() throws MarshallingException {
        Json j = Json.Builder.create()
                .withProperty("prop", Json.UndefinedValue)
                .finish();

        String serialized = j.serialize();
        Json deserialized = Json.deserialize(serialized);
        assertEquals(serialized, deserialized.serialize());
        assertTrue(deserialized.get("prop") instanceof Json._UndefinedValue);
    }
}
