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

    public static Command getCommand(Main.CommandType commandType) {
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
    public PerformanceData call() throws Exception {

        String threadName = Thread.currentThread().getName();
        logger.debug(String.format("The thread %s started running", threadName));

        try (FileWriter writer = new FileWriter(Main.STATS_FILE, true)) {

            for (int i = 0; i < Main.ROUNDS; i++) {
                logger.debug(String.format("Running command %d th iteration in thread  %s ", i, threadName));
                TimeWatch t = TimeWatch.start();
                Command cm = getCommand(Main.commandType);
                cm.execute(state);
                logger.debug(String.format("Finished command %d th iteration in thread  %s in (%d ms) ", i, threadName, t.time()));

                //writer.append(String.format("%s, %s, %d", threadName, cm.toString(), t.time())).append(System.lineSeparator());
                writer.append(String.format("%s, %d", threadName, t.time())).append(System.lineSeparator());
            }
            writer.flush();
        }
        return new PerformanceData();
    }
}
