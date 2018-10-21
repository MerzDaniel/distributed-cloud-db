package ui;

public class Util {
    public static void writeLine(String line) {
        System.out.print("EchoClient> ");
        System.out.println(line.replaceAll("\n", ""));
    }
}
