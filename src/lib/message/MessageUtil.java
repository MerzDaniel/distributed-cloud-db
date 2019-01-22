package lib.message;

import lib.message.kv.KVMessage;

/**
 * This class provides utility methods to check the validity of {@code key} and {@code value} of a {@link KVMessage}
 */
public class MessageUtil {
    /**
     * Returns whether a given {@code key} is valid or not
     * @param key the key to be validated
     * @return the validity
     */
    public static boolean isValidKey(String key) {
        if (key == null) return false;
        return key.length() <= 20;
    }

    /**
     * Returns whether a given {@code value} is valid or not
     *
     * @param value the value to be validated
     * @return the validity
     */
    public static boolean isValidValue(String value) {
        if (value == null) return true;
        return value.length() <= 120000;
    }
}
