package ui;

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
}
