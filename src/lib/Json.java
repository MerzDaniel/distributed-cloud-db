package lib;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Json {
    private List<Property> properties = new ArrayList<>();

    abstract static class Property {
        public String name;
        public abstract String serialize();
    }

    static class JsonProperty extends Property {
        public Json value;

        public JsonProperty(String name, Json value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String serialize() {
            return name + ":" + value.serialize();
        }
    }

    static class StringProperty extends Property {
        public String value;

        public StringProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String serialize() {
            return name + ":" + value;
        }
    }

    public String serialize() {
        return String.format("{%s}", properties.stream().map(p -> p.serialize()).collect(Collectors.joining(",")));
    }

    public static Json deserialize(String s) {
        return null;
    }

    public void addProperty(Property property){
        properties.add(property);
    }
}
