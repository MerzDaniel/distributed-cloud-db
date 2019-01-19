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

    static class ComplexProperty extends Property {
        public Json value;

        public ComplexProperty(String name, Json value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String serialize() {
            return name + ":" + value.serialize();
        }
    }

    static class SimpleProperty extends Property {
        public String value;

        public SimpleProperty(String name, String value) {
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
