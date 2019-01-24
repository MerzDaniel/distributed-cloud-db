package lib.json;

import java.util.Collection;
import java.util.LinkedList;

public class JsonParser {

    String input;
    int index = 0;
    final char specialChars[] = {'{', '}', '[', ']', ','};

    public static Json parse(String s) throws JsonFormatException {
        return new JsonParser(s)._parse();
    }

    private char lookahead() { return input.charAt(index);}

    private JsonParser(String s) {
        input = s;
    }

    private Json _parse() throws JsonFormatException {
        Json result = parseObject();
        if (index < input.length()) throw new JsonFormatException();

        return result;
    }

    private Json parseObject() throws JsonFormatException {
        consumeExpectedChar('{');

        Json result = new Json();
        if (lookahead() == '}') return result;
        result.properties.addAll(parseProperties());

        consumeExpectedChar('}');
        return result;
    }

    private Collection<? extends Json.Property> parseProperties() throws JsonFormatException {
        LinkedList<Json.Property> properties = new LinkedList<>();
        while (true) {
            Json.Property next = parseProperty();
            properties.add(next);

            if (lookahead() != ',') break;
            consomueChar();
        }
        return properties;
    }

    private Json.Property parseProperty() throws JsonFormatException {
        String key = consumeCharsTillBefore(':');
        consumeExpectedChar(':');
        Json.PropertyValue pv = parsePropertyValue();
        return new Json.Property(key, pv);
    }

    private Json.PropertyValue parsePropertyValue() throws JsonFormatException {
        if (lookahead() == '{') return new Json.JsonValue(parseObject());
        if (lookahead() == '[') return new Json.ArrayValue(parseArray());

        return new Json.StringValue(consumeCharsTillBeforeSpecialChar());
    }

    private Json.PropertyValue[] parseArray() throws JsonFormatException {
        LinkedList<Json.PropertyValue> propVals = new LinkedList<>();
        consumeExpectedChar('[');
        while(true) {
            propVals.add(parsePropertyValue());
            if (lookahead() != ',') break;
            consumeExpectedChar(',');
        }
        consumeExpectedChar(']');
        return (Json.PropertyValue[]) propVals.toArray();
    }

    private char consomueChar() {
        return input.charAt(index++);
    }

    private char consumeExpectedChar(char c) throws JsonFormatException {
        if (lookahead() != c) throw new JsonFormatException();
        return consomueChar();
    }
    private String consumeCharsTillBefore(char c) {
        int indexOfChar = input.indexOf(c, index);
        String result = input.substring(index, indexOfChar);
        index = indexOfChar;

        return result;
    }
    private String consumeCharsTillBeforeSpecialChar() {
        StringBuilder stringBuilder = new StringBuilder();
        while(true) {
            char current = lookahead();
            boolean isSpecial = false;
            for (char special : specialChars)
                if (special == current) { isSpecial = true; break; }

            if (isSpecial) break;
            stringBuilder.append(consomueChar());
        }
        return stringBuilder.toString();
    }
}
