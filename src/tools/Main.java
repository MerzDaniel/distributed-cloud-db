package tools;

import lib.StreamUtils;
import tools.util.EnroneBenchmarkDataLoader;
import tools.util.PerformanceData;

import java.io.File;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import static tools.util.EnroneBenchmarkDataLoader.Loader;

/**
 * This class is only used for getting performance measures for KVClient
 */
public class Main {
    final static int ROUNDS = 1;
    final static int NO_OF_CLIENTS = 3;
    final static double PERCENTAGE_WRITES = 0.2;
    final static CommandType commandType = CommandType.PUT_NO_KEY_CONFLICTS;

    static ExecutorService executorService = Executors.newFixedThreadPool(NO_OF_CLIENTS);

    final static File STATS_DIRECTORY = new File(Paths.get("stats").toUri());
    final static File STATS_FILE = new File(Paths.get(STATS_DIRECTORY.toString(), "stats.csv").toUri());

    final static File TEST_DATA_DIRECTORY = new File(Paths.get("resources/till-beck-140mb").toUri());
    final static Stream<AbstractMap.SimpleEntry<String, Loader>> testDataStream = EnroneBenchmarkDataLoader.loadData(TEST_DATA_DIRECTORY, true);

    enum CommandType {
        GET,
        PUT_NO_KEY_CONFLICTS,
        PUT_WITH_KEY_CONFLICTS,
        DELETE
    }

    public static void main(String[] args) {
        runTest();
        executorService.shutdown();
    }

    private static AbstractMap.SimpleEntry<String, Loader> getKeyValue() {
        Iterator<AbstractMap.SimpleEntry<String, Loader>> it = testDataStream.iterator();

        if (it.hasNext()) {
            return it.next();
        }

        return null;
    }

    public static void runTest(){
        Stream s = StreamUtils.asStream(new Iterator<PerformanceData>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public PerformanceData next() {
                try {
                    return new KVTestClient(TEST_DATA_DIRECTORY, PERCENTAGE_WRITES).call();
                } catch (Exception e) {
                    e.printStackTrace();
                    return new PerformanceData();
                }
            }
        }, false);
        s.limit(NO_OF_CLIENTS).parallel();
    }

}
