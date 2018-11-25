package tools.util;

import lib.StreamUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public final class EnroneBenchmarkDataLoader {
    static Logger logger = LogManager.getLogger(EnroneBenchmarkDataLoader.class);

    public static Stream<AbstractMap.SimpleEntry<String, Loader>> loadData(File file, boolean uniqueKeys) {
        return StreamUtils.asStream(createIterator(file, uniqueKeys), false);
    }

    private static Iterator<AbstractMap.SimpleEntry<String, Loader>> createIterator(File file, boolean uniqueKeys) {
        return new Iterator<AbstractMap.SimpleEntry<String, Loader>>() {
            List<File> fileList = new LinkedList<>(Arrays.asList(file));
            AbstractMap.SimpleEntry<String, Loader> next = null;

            @Override
            public boolean hasNext() {
                if (next != null) return true;
                if (fileList.isEmpty()) return false;

                File current = fileList.remove(fileList.size() - 1);
                while (current.isDirectory()) {
                    List<File> files = Arrays.asList(current.listFiles());
                    Collections.reverse(files);
                    fileList.addAll(files);
                    current = fileList.remove(fileList.size() - 1);
                }

                String randomKeyPrefix = String.valueOf((int)new Random().nextDouble() * 10);
                String key = randomKeyPrefix + (uniqueKeys ? current.getAbsolutePath() : current.getName());
                next = new AbstractMap.SimpleEntry<>(key, new Loader((current)));
                return true;
            }

            @Override
            public AbstractMap.SimpleEntry<String, Loader> next() {
                AbstractMap.SimpleEntry<String, Loader> a = next;
                next = null;
                return a;
            }
        };
    }

    public static class Loader {
        private File file;

        public Loader(File file) {

            this.file = file;
        }

        public String Load() {
            try {
                return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.warn("File could not be loaded", e);
                return "no value...";
            }
        }
    }

}
