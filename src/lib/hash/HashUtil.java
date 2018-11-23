package lib.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class provides the methods to get the consistent hashing for a given string
 */
public class HashUtil {

    /**
     * Get the consistent hash for the given {@code key}
     *
     * @param key key
     * @return consistent hash as a {@Link BigInteger}
     * @throws NoSuchAlgorithmException if the hashing algorithm is not existing
     */
    public static BigInteger getHash(String key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(key.getBytes());
        String hashHexString = getHexString(digest).toUpperCase();
        return new BigInteger(hashHexString, 16);
    }

    private static String getHexString(byte[] data) {
        final char[] hexCode = "0123456789ABCDEF".toCharArray();
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }
}

