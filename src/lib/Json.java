package lib;

import lib.message.exception.MarshallingException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Json {
    // TODO GRAPH: Write working parser for nested objects
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
        public static Property deserialize(String s) throws MarshallingException {
            String split[] = s.split(":", 2);
            PropertyValue val;
            if (split[1].length() == 0) val = UndefinedValue;
            else val = PropertyValue.deserialize(split[1]);

            return new Property(split[0], val);
        }
    }

    public abstract static class PropertyValue {
        public abstract String serialize();
        public static PropertyValue deserialize(String s) throws MarshallingException {
            if (s.startsWith("{")) return new JsonValue(Json.deserialize(s));
            if (s.startsWith("[")) {
                PropertyValue values[] = (PropertyValue[]) Arrays.stream(s.substring(1, s.length() - 2).split(",")).map(
                        v -> {
                            try {
                                return PropertyValue.deserialize(v);
                            } catch (MarshallingException e) {
                                return null;
                            }
                        }
                ).collect(Collectors.toList()).toArray();
                return new ArrayValue(values);
            }

            return new StringValue(s);
        }
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
        if (!(s.startsWith("{") && s.endsWith("}"))) throw new MarshallingException("Unexpected Format: " + s);

        Json result = new Json();
        if (s.length() > 2) {
            String split[] = s.substring(1, s.length()-1).split(",");
            for (String prop : split) {
                result.setProperty(Property.deserialize(prop));
            }
        }
        return result;
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
