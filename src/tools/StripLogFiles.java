package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class StripLogFiles {
    final static int TARGET_FILE_SIZE = 1024 * 1024 * 2;

    public static void main(String[] args) throws IOException {
        String files[] = {"client.log", "ecs.log", "server.log", "tools.log"};
        for (String f : files) {
            File file = new File(Paths.get("C:\\Users\\daniel\\git\\gr10\\logs", f).toString());
//            r.seek(r.length() - 10000000);
            if (!file.exists()) continue;
            if (file.length() < TARGET_FILE_SIZE) continue;

            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.skip(file.length() - TARGET_FILE_SIZE);
            byte buf[] = new byte[TARGET_FILE_SIZE];
            fileInputStream.read(buf);
            String s = new String(buf);
            fileInputStream.close();

            new FileWriter(file).write(s);
        }
    }
}
