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

    public Property findProp(String key) {
        for (Property p : properties)
            if (p.key.equals(key)) return p;

        return null;
    }

    public void setProperty(Property property){
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
            return key + ":" + value.serialize();
        }
    }

    public abstract static class PropertyValue {
        public abstract String serialize();
    }

    public static class StringValue extends PropertyValue {
        public StringValue(String value) {
            this.value = value;
        }

        public String value;

        @Override
        public String serialize() {
            return value;
        }
    }

    public static class JsonValue extends PropertyValue{
        public JsonValue(Json value) {
            this.value = value;
        }

        Json value;

        @Override
        public String serialize() {
            return value.serialize();
        }
    }

    public static class ArrayValue extends PropertyValue {
        PropertyValue[] values;

        public ArrayValue(PropertyValue[] values) {
            this.values = values;
        }

        @Override
        public String serialize() {
            return String.join(",", Arrays.stream(values).map(v -> v.serialize()).collect(Collectors.toList()));
        }
    }

    public static final PropertyValue UndefinedValue = new _UndefinedValue();

    private static class _UndefinedValue extends PropertyValue {
        @Override
        public String serialize() {
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
        public Json finish() { return json; }


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
    }

    public Property getProperty(String key) {
        return properties.stream().filter(it -> it.key.equals(key)).findFirst().get();
    }
}
