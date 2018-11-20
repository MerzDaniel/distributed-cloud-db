package lib.benchmark;

import lib.StreamUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EnroneBenchmarkDataLoader {
    static Logger logger = LogManager.getLogger(EnroneBenchmarkDataLoader.class);

    public static Stream<AbstractMap.SimpleEntry<String, String>> loadData(File file, boolean uniqueKeys) {
        return StreamUtils.asStream(createIterator(file, uniqueKeys), false);
    }

    private static Iterator<AbstractMap.SimpleEntry<String, String>> createIterator(File file, boolean uniqueKeys) {
        return new Iterator<AbstractMap.SimpleEntry<String, String>>() {
            List<File> fileList = new LinkedList<>(Arrays.asList(file));
            AbstractMap.SimpleEntry<String, String> next = null;

            @Override
            public boolean hasNext() {
                if (next != null) return true;
                if (fileList.isEmpty()) return false;

                File current = fileList.remove(fileList.size()-1);
                while (current.isDirectory()) {
                    List<File> files = Arrays.asList(current.listFiles());
                    Collections.reverse(files);
                    fileList.addAll(files);
                    current = fileList.remove(fileList.size()-1);
                }

                try {
                    String key = uniqueKeys ? current.getAbsolutePath() : current.getName();
                    String value = new String(Files.readAllBytes(current.toPath()), StandardCharsets.UTF_8);
                    next = new AbstractMap.SimpleEntry<>(key, value);
                    return true;
                } catch (IOException e) {
                    logger.warn("Error while reading file", e);
                    return false;
                }
            }

            @Override
            public AbstractMap.SimpleEntry<String, String> next() {
                AbstractMap.SimpleEntry<String, String> a = next;
                next = null;
                return a;
            }
        };
    }


}
