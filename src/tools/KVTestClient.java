package tools;

import client.store.KVStore;
import client.ui.ApplicationState;
import lib.TimeWatch;
import lib.message.KVMessage;
import lib.message.MarshallingException;
import lib.metadata.KVServerNotFoundException;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import tools.util.Command;
import tools.util.PerformanceData;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;

class KVTestClient implements Callable {
    Logger logger = LogManager.getLogger(KVTestClient.class);
    KVStore kvStore;

    private void init() {
        ServerData sd = new ServerData("kitten-" + new Random().nextInt(), "127.0.0.1", 50000);
        kvStore = new KVStore(
                new KVStoreMetaData(Arrays.asList(sd))
        );
    }

    public KVTestClient() {
        this.init();
    }

    @Override
    public PerformanceData call() {
        PerformanceData perfData = new PerformanceData();

        String threadName = Thread.currentThread().getName();
        logger.debug(String.format("The thread %s started running", threadName));

        for (int i = 0; i < Main.ROUNDS; i++) {
            logger.debug(String.format("Running command %d th iteration in thread  %s ", i, threadName));

            String key = "", value = "";
            Command c = new Command(key, value, "GET");

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

            logger.debug(String.format("Finished command %d th iteration in thread  %s in (%d ms) ", i, threadName, t.time()));
        }

        return perfData;
    }
}
