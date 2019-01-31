package tools;

import client.store.KVStore;
import lib.TimeWatch;
import lib.message.kv.KVMessage;
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
    private double percentageWrites;

    private void init() {
        String host = "localhost";
//        String host = "192.168.178.27";
        ServerData sd = new ServerData("not initialized", host, 50000);
        kvStore = new KVStore(
                new KVStoreMetaData(Arrays.asList(sd))
        );
    }

    public KVTestClient(File pathToPerformance, double percentageWrites) {
        this.pathToPerformance = pathToPerformance;
        this.percentageWrites = percentageWrites;
        this.init();
    }

    @Override
    public PerformanceData call() {
        PerformanceData perfData = new PerformanceData();
        Stream<AbstractMap.SimpleEntry<String, EnroneBenchmarkDataLoader.Loader>> dataStream =
                EnroneBenchmarkDataLoader.loadData(pathToPerformance, false);

        TimeWatch wholeTime = TimeWatch.start();

        dataStream.limit(PerformanceMesurement.ROUNDS).forEach(data -> {
            String key = data.getKey(), value = data.getValue().Load();
            String command = new Random().nextDouble() > percentageWrites ? "GET" : "PUT";
            Command c = new Command(key, value, command);

            TimeWatch requestWatch = TimeWatch.start();
            String response;
            try {
                KVMessage result = null;
                result = c.run(kvStore);
                response = result.getStatus().name();
            } catch (KVServerNotFoundException e) {
                response = "SERVER_NOT_FOUND";
                logger.warn("Server not found", e);
            } catch (Exception e) {
                response = "ERROR";
                logger.warn("ERROR: ", e);
            }

            PerformanceData.Entry e = new PerformanceData.Entry();
            e.method = c.command;
            e.time = requestWatch.time();
            e.result = response;

            perfData.entries.add(e);

        });

        perfData.elapsedTime = wholeTime.time();
        return perfData;
    }
}
