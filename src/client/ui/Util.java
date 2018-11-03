package client.ui;

/**
 * Utility class for the application
 */
public class Util {
    /**
     * Writes a line to System.out using the 'EchoClient>' prefix
     */
    public static void writeLine(String line) {
        System.out.print("EchoClient> ");
        System.out.println(line.replaceAll("\n", ""));
    }
    public static boolean isValidKey(String key) {
        if (key == null) return false;
        return key.length() <= 20;
    }
    public static boolean isValidValue(String value) {
        return value.length() <= 120000;
    }
}
