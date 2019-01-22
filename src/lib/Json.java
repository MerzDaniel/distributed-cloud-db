package lib;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Json {
    public List<Property> properties = new LinkedList<>();

    public abstract static class Property {
        public String key;
        public abstract String serialize();
    }

    public static class JsonProperty extends Property {
        public Json value;

        public JsonProperty(String key, Json value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String serialize() {
            return key + ":" + value.serialize();
        }
    }

    public static class StringProperty extends Property {
        public String value;

        public StringProperty(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String serialize() {
            return key + ":" + value;
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
