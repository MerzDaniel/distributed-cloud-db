package lib.json;

import lib.message.exception.MarshallingException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Json {
    public List<Property> properties = new LinkedList<>();

    public PropertyValue get(String key) {
        Property p = findProp(key);
        if (p == null) return null;

        return p.value;
    }
    public void set(String key, PropertyValue value) {
        set(new Property(key, value));
    }
    public void set(Property p) {
        for (Property property : properties) {
            if (property.key.equals(p.key)){
                properties.remove(property);
                break;
            }
        }
        properties.add(p);
    }

    public String prettyPrint() {
        return "{\n" +
                prettyPrint("  ") + "\n" +
                "}";
    }

    private String prettyPrint(String offset) {
        if (properties.size() == 0) return "";
        return
                properties.stream()
                        .map(p -> p.prettyPrint(offset))
                        .collect(Collectors.joining(",\n"));

    }

    public Property findProp(String key) {
        return properties.stream().filter(it -> it.key.equals(key)).findAny().orElse(null);
    }

    public void setProperty(Property property) {
        Property oldProp = findProp(property.key);
        if (oldProp != null) properties.remove(oldProp);

        properties.add(property);
    }

    public static class Property {
        public String key;
        public PropertyValue value;

        public Property(String key, PropertyValue value) {
            this.key = key;
            this.value = value;
        }

        public String serialize() {
            return "\"" + key + "\":" + value.serialize();
        }

        public String prettyPrint(String offset) {
            return offset + key + ": " + value.prettyPrint(offset);
        }
    }

    public abstract static class PropertyValue {
        public abstract String serialize();

        public abstract String prettyPrint(String s);
    }

    public static class StringValue extends PropertyValue {
        public StringValue(String value) {
            this.value = value;
        }

        public String value;

        @Override
        public String serialize() {
            return "\"" + value + "\"";
        }

        @Override
        public String prettyPrint(String s) {
            return value;
        }
    }

    public static class JsonValue extends PropertyValue {
        public JsonValue(Json value) {
            this.value = value;
        }

        public Json value;

        @Override
        public String serialize() {
            return value.serialize();
        }

        @Override
        public String prettyPrint(String offset) {
            if (value.properties.size() == 0) return "{}";
            return "{\n" +
                    value.prettyPrint(offset + "  ") + "\n" +
                    offset + "}";
        }
    }

    public static class ArrayValue extends PropertyValue {
        public List<PropertyValue> values;

        public ArrayValue(List<PropertyValue> values) {
            this.values = values;
        }

        public ArrayValue(PropertyValue[] values) {
            this.values = Arrays.asList(values);
        }

        public ArrayValue() { this.values = new LinkedList<>(); }

        @Override
        public String serialize() {
            return String.format("[%s]", String.join(",", values.stream().map(v -> v.serialize()).collect(Collectors.toList())));
        }

        @Override
        public String prettyPrint(String offset) {
            if (values.size() == 0) return "[]";
            String prettyElements = values.stream().map(v -> offset + "  " + v.prettyPrint(offset + "  ")).collect(Collectors.joining(",\n"));
            return "[\n" +
                    prettyElements + ",\n" +
                    offset + "]";
        }
    }

    public static final PropertyValue UndefinedValue = new _UndefinedValue();

    public static class _UndefinedValue extends PropertyValue {
        protected _UndefinedValue() {
        }

        @Override
        public String serialize() {
            return "\"\"";
        }

        @Override
        public String prettyPrint(String s) {
            return "";
        }
    }

    public String serialize() {
        return String.format("{%s}", properties.stream().map(p -> p.serialize()).collect(Collectors.joining(",")));
    }

    public static Json deserialize(String s) throws MarshallingException {
        try {
            return JsonParser.parse(s);
        } catch (JsonFormatException e) {
            throw new MarshallingException(e);
        }
    }

    public static class Builder {
        private Json json = new Json();

        private Builder() {

        }

        public static Builder create() {
            return new Builder();
        }

        public Json finish() {
            return json;
        }


        public Builder withStringProperty(String key, String value) {
            json.setProperty(new Property(key, new StringValue(value)));
            return this;
        }

        public Builder withJsonProperty(String key, Json value) {
            json.setProperty(new Property(key, new JsonValue(value)));
            return this;
        }

        public Builder withProperty(String propKey, PropertyValue propVal) {
            json.setProperty(new Property(propKey, propVal));
            return this;
        }

        public Builder withArrayProperty(String arrPropKey, PropertyValue[] arrPropVal) {

            json.setProperty(new Property(arrPropKey, new ArrayValue(Arrays.asList((arrPropVal)))));
            return this;
        }

        public Builder withStringArrayProperty(String arrPropKey, List<String> list) {

            json.setProperty(new Property(
                    arrPropKey,
                    new ArrayValue(list.stream()
                            .map(s -> new StringValue(s))
                            .collect(Collectors.toList())
                    )
            ));
            return this;
        }

        public Builder withUndefinedProperty(String key) {
            json.setProperty(new Property(key, UndefinedValue));
            return this;
        }
    }

    public Property getProperty(String key) {
        return properties.stream().filter(it -> it.key.equals(key)).findFirst().get();
    }
}
