package tools;

import client.store.KVStore;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import tools.util.EnroneBenchmarkDataLoader;

import java.io.File;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.stream.Stream;

public class FillupDb {
    public static void main(String[] args) {
        File TEST_DATA_DIRECTORY = new File(args[0]);
        int DATASET_COUNT = Integer.parseInt(args[1]);

        ServerData sd = new ServerData("not initialized", "localhost", 50000);
        Stream<AbstractMap.SimpleEntry<String, EnroneBenchmarkDataLoader.Loader>> dataStream =
                EnroneBenchmarkDataLoader.loadData(TEST_DATA_DIRECTORY, false);
        dataStream.limit(DATASET_COUNT)
                .parallel()
                .forEach(data -> {
                    KVStore kvStore = new KVStore(
                            new KVStoreMetaData(Arrays.asList(sd))
                    );
                    String key = data.getKey(), value = data.getValue().Load();
                    try {
                        kvStore.put(key, value);
                    } catch (Exception e) {
                        System.out.println("ERROR");
                        e.printStackTrace();
                    }
                });
    }
}
