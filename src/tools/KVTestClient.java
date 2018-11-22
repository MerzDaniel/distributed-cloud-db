package tools;

import client.store.KVStore;
import lib.TimeWatch;
import lib.message.KVMessage;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import tools.util.Command;
import tools.util.EnroneBenchmarkDataLoader;
import tools.util.PerformanceData;

import java.io.File;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

class KVTestClient implements Callable {
    Logger logger = LogManager.getLogger(KVTestClient.class);
    KVStore kvStore;
    private File pathToPerformance;

    private void init() {
        ServerData sd = new ServerData("kitten-" + new Random().nextInt(), "127.0.0.1", 50000);
        kvStore = new KVStore(
                new KVStoreMetaData(Arrays.asList(sd))
        );
    }

    public KVTestClient(File pathToPerformance) {
        this.pathToPerformance = pathToPerformance;
        this.init();
    }

    @Override
    public PerformanceData call() {
        PerformanceData perfData = new PerformanceData();
        Stream<AbstractMap.SimpleEntry<String, EnroneBenchmarkDataLoader.Loader>> dataStream =
                EnroneBenchmarkDataLoader.loadData(pathToPerformance, false);

        dataStream.limit(Main.ROUNDS).forEach(data -> {

            String key = data.getKey(), value = data.getValue().Load();
            Command c = new Command(key, value, "PUT");

            TimeWatch t = TimeWatch.start();
            String response;
            try {
                KVMessage result = null;
                result = c.run(kvStore);
                response = result.getStatus().name();
            } catch (KVServerNotFoundException e) {
                e.printStackTrace();
                response = "GET_NotFound";
            } catch (Exception e) {
                e.printStackTrace();
                response = "ERROR";
            }

            PerformanceData.Entry e = new PerformanceData.Entry();
            e.method = c.command;
            e.time = t.time();
            e.result = response;

            perfData.entries.add(e);

        });

        return perfData;
    }
}
