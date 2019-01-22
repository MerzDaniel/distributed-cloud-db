package lib;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class JsonTest {

    @Test
    public void testSerialize() {
        Json j = Json.Factory.create().withStringProperty("name", "Jhon").finish();
        String s = j.serialize();
        assertEquals("{name:Jhon}", s);
    }

    @Test
    public void testSerializeMultipleProperties() {
        Json j = Json.Factory.create()
                .withStringProperty("name", "Jhon")
                .withStringProperty("age", "35")
                .withStringProperty("country", "USA")
                .finish();
        String s = j.serialize();
        assertEquals("{name:Jhon,age:35,country:USA}", s);
    }

    @Test
    public void testSerializeComplexProperties() {

        Json f = Json.Factory.create()
                .withStringProperty("name", "Khan")
                .withStringProperty("age", "34")
                .finish();

        Json j = Json.Factory.create()
                .withStringProperty("name", "Jhon")
                .withStringProperty("age", "35")
                .withStringProperty("country", "USA")
                .withJsonProperty("friend", f)
                .finish();
        
        String s = j.serialize();
        assertEquals("{name:Jhon,age:35,country:USA,friend:{name:Khan,age:34}}", s);
    }
}
