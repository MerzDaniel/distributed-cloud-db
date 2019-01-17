package lib;

import java.util.List;

public class Json {
    class Property {
        public String name;
        public Json value;
        public String serialize() {
            return name + ":" + value.serialize();
        }
    }
    private List<Property> properties;

    public String serialize() {
        return String.format("{%s}", properties.map(p->p.serialize()));
    }

    public static Json deserialize(String s) {

    }
}
