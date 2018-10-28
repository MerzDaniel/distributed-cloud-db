package lib.message;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KVMessageUnmarshaller {
    static Logger logger = LogManager.getLogger(KVMessageUnmarshaller.class);

    private KVMessageUnmarshaller(){}

    public static KVMessage unmarshall(String kvMessageString) throws UnmarshallException{
        KVMessage.StatusType status = null;
        String key = null;
        String value = null;

        try {
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
        } catch (Exception e) {
            logger.warn("Exception while parsing message: '" + kvMessageString + "'", e);
            throw new UnmarshallException(e);
        }
    }

    private static String removeEscapeCharacters(String string) {
        if(string == null || string.length() == 0){
            return null;
        }
        return string.replaceAll("//", "/").replaceAll("/<", "<").replaceAll("/,", ",").replaceAll("/>", ">");
    }
}
