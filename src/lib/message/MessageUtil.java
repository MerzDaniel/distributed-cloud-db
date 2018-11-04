package lib.message;

public class MessageUtil {

    public static boolean isValidKey(String key) {
        if (key == null) return false;
        return key.length() <= 20;
    }


    public static boolean isValidValue(String value) {
        return value.length() <= 120000;
    }
}