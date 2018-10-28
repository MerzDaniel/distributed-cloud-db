package lib.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KVMessageUnmarshaller {

    private KVMessageUnmarshaller(){}

    public static KVMessage unmarshall(String kvMessageString) {
        KVMessage.StatusType status = null;
        String key = null;
        String value = null;

        String statusPattern = "[^/]+(?=<)";
        String keyPattern = "(?<=<).*([^/]//(//)*|[^/])(?=,)";
        String valuePattern = "(?<=(//|[^/]))(?<=,).*(?=[^/])(?=>)";

        Pattern pattern = Pattern.compile(statusPattern);
        Matcher matcher = pattern.matcher(kvMessageString);
        while (matcher.find()) {
            status = KVMessage.StatusType.valueOf(matcher.group());
            kvMessageString = kvMessageString.substring(matcher.end());
            break;
        }

        pattern = Pattern.compile(keyPattern);
        matcher = pattern.matcher(kvMessageString);
        while (matcher.find()) {
            key = matcher.group();
            kvMessageString = kvMessageString.substring(matcher.end());
            break;
        }

        pattern = Pattern.compile(valuePattern);
        matcher = pattern.matcher(kvMessageString);
        while (matcher.find()) {
            value = matcher.group();
            break;
        }

        return new KVMessageImpl(removeEscapeCharacters(key), removeEscapeCharacters(value), status);
    }

    private static String removeEscapeCharacters(String string) {
        return string.replaceAll("//", "/").replaceAll("/<", "<").replaceAll("/,", ",").replaceAll("/>", ">");
    }
}
