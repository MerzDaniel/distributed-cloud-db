package lib;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class JsonTest {

    @Test
    public void testSerialize(){
        Json j = new Json();
        j.addProperty(new Json.SimpleProperty("name", "Jhon"));
        String s = j.serialize();
        assertEquals("{name:Jhon}", s);
    }

    @Test
    public void testSerializeMultipleProperties(){
        Json j = new Json();
        j.addProperty(new Json.SimpleProperty("name", "Jhon"));
        j.addProperty(new Json.SimpleProperty("age", "35"));
        j.addProperty(new Json.SimpleProperty("country", "USA"));
        String s = j.serialize();
        assertEquals("{name:Jhon,age:35,country:USA}", s);
    }

    @Test
    public void testSerializeComplexProperties(){
        Json j = new Json();
        j.addProperty(new Json.SimpleProperty("name", "Jhon"));
        j.addProperty(new Json.SimpleProperty("age", "35"));
        j.addProperty(new Json.SimpleProperty("country", "USA"));

        Json f = new Json();
        f.addProperty(new Json.SimpleProperty("name", "Khan"));
        f.addProperty(new Json.SimpleProperty("age", "34"));

        j.addProperty(new Json.ComplexProperty("friend", f));
        String s = j.serialize();
        assertEquals("{name:Jhon,age:35,country:USA,friend:{name:Khan,age:34}}", s);
    }
}
