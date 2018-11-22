package tools;

import client.store.KVStore;
import client.ui.ApplicationState;
import client.ui.Command;
import client.ui.CommandParser;
import client.ui.commands.GetCommand;
import client.ui.commands.PutCommand;
import lib.TimeWatch;
import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import tools.util.PerformanceData;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;

class KVTestClient implements Callable {
    Logger logger = LogManager.getLogger(KVTestClient.class);
    ApplicationState state;

    private void init() {
        ServerData sd = new ServerData("kitten-" + new Random().nextInt(), "127.0.0.1", 50000);
        state = new ApplicationState(
                new KVStore(
                        new KVStoreMetaData(Arrays.asList(sd))
                )
        );
    }

    public KVTestClient() {
        this.init();
    }

    @Override
    public PerformanceData call() {

        String threadName = Thread.currentThread().getName();
        logger.debug(String.format("The thread %s started running", threadName));

        for (int i = 0; i < Main.ROUNDS; i++) {
            logger.debug(String.format("Running command %d th iteration in thread  %s ", i, threadName));

            String key, value;

            TimeWatch t = TimeWatch.start();
//            cm.execute(state);
            logger.debug(String.format("Finished command %d th iteration in thread  %s in (%d ms) ", i, threadName, t.time()));

            //writer.append(String.format("%s, %s, %d", threadName, cm.toString(), t.time())).append(System.lineSeparator());
        }

        return new PerformanceData();
    }
}
