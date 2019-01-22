package lib;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class JsonTest {

    @Test
    public void testSerialize(){
        Json j = new Json();
        j.addProperty(new Json.StringProperty("name", "Jhon"));
        String s = j.serialize();
        assertEquals("{name:Jhon}", s);
    }

    @Test
    public void testSerializeMultipleProperties(){
        Json j = new Json();
        j.addProperty(new Json.StringProperty("name", "Jhon"));
        j.addProperty(new Json.StringProperty("age", "35"));
        j.addProperty(new Json.StringProperty("country", "USA"));
        String s = j.serialize();
        assertEquals("{name:Jhon,age:35,country:USA}", s);
    }

    @Test
    public void testSerializeComplexProperties(){
        Json j = new Json();
        j.addProperty(new Json.StringProperty("name", "Jhon"));
        j.addProperty(new Json.StringProperty("age", "35"));
        j.addProperty(new Json.StringProperty("country", "USA"));

        Json f = new Json();
        f.addProperty(new Json.StringProperty("name", "Khan"));
        f.addProperty(new Json.StringProperty("age", "34"));

        j.addProperty(new Json.JsonProperty("friend", f));
        String s = j.serialize();
        assertEquals("{name:Jhon,age:35,country:USA,friend:{name:Khan,age:34}}", s);
    }
}
