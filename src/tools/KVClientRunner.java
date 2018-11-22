package tools;

import client.store.KVStore;
import client.ui.ApplicationState;
import client.ui.Command;
import client.ui.CommandParser;
import client.ui.commands.GetCommand;
import client.ui.commands.PutCommand;
import lib.StreamUtils;
import lib.TimeWatch;
import lib.benchmark.EnroneBenchmarkDataLoader;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import static lib.benchmark.EnroneBenchmarkDataLoader.Loader;

/**
 * This class is only used for getting performance measures for KVClient
 */
public class KVClientRunner {


    final static int ROUNDS = 100;
    final static int NO_OF_CLIENTS = 3;
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
//        for(int i = 0; i < NO_OF_CLIENTS; i++) {
//            Future<Boolean> f = executorService.submit(new KVTestClient());
//        }
        Stream s = StreamUtils.asStream(new Iterator<Boolean>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Boolean next() {
                try {
                    return new KVTestClient().call();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }, true);
        s.limit(NO_OF_CLIENTS).count();
    }

    static class KVTestClient implements Callable {
        Logger logger;
        ApplicationState state;
        CommandParser commandParser;

        private void init() {
            logger = LogManager.getLogger(KVTestClient.class);

            ServerData sd = new ServerData("kitten-" + new Random().nextInt(), "127.0.0.1", 50000);
            state = new ApplicationState(
                    new KVStore(
                            new KVStoreMetaData(Arrays.asList(sd))
                    )
            );
            commandParser = new CommandParser();
        }

        public KVTestClient(){
            this.init();
        }

        public static Command getCommand(CommandType commandType) {
            switch (commandType) {
                case GET:
                    return new GetCommand("" + Math.random());
                case PUT_NO_KEY_CONFLICTS:
                    return new PutCommand("" + Math.random(), "" + Math.random());
                case PUT_WITH_KEY_CONFLICTS:
                    return new PutCommand("", "");
                case DELETE:
                    return new PutCommand("", "");
                default:
                    return new GetCommand("");

            }
        }

        @Override
        public Boolean call() throws Exception {

            String threadName = Thread.currentThread().getName();
            logger.debug(String.format("The thread %s started running", threadName));

            try (FileWriter writer = new FileWriter(STATS_FILE, true)) {

                for (int i = 0; i < ROUNDS; i++) {
                    logger.debug(String.format("Running command %d th iteration in thread  %s ", i, threadName));
                    TimeWatch t = TimeWatch.start();
                    Command cm = getCommand(commandType);
                    cm.execute(state);
                    logger.debug(String.format("Finished command %d th iteration in thread  %s in (%d ms) ", i, threadName, t.time()));

                    //writer.append(String.format("%s, %s, %d", threadName, cm.toString(), t.time())).append(System.lineSeparator());
                    writer.append(String.format("%s, %d", threadName, t.time())).append(System.lineSeparator());
                }
                writer.flush();
            }
            return true;
        }
    }
}
