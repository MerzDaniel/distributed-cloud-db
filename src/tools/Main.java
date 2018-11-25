package tools;

import lib.message.KVMessage;
import tools.util.EnroneBenchmarkDataLoader;
import tools.util.PerformanceData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static tools.util.EnroneBenchmarkDataLoader.Loader;

/**
 * This class is only used for getting performance measures for KVClient
 */
public class Main {
    final static int ROUNDS = 100;
    final static int NO_OF_CLIENTS = 1;
    final static double PERCENTAGE_WRITES = 0.2;

    static ExecutorService executorService = Executors.newFixedThreadPool(NO_OF_CLIENTS);

    final static File STATS_DIRECTORY = new File(Paths.get("stats").toUri());
    final static File STATS_FILE = new File(Paths.get(STATS_DIRECTORY.toString(), "stats.csv").toUri());

    final static File TEST_DATA_DIRECTORY = new File(Paths.get("resources/till-beck-140mb").toUri());
    final static Stream<AbstractMap.SimpleEntry<String, Loader>> testDataStream = EnroneBenchmarkDataLoader.loadData(TEST_DATA_DIRECTORY, true);

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
        List<PerformanceData> pfList = new ArrayList<>();
        System.out.println("Please bare with us, tests are being running............................");

        List<Future<PerformanceData>> list = new LinkedList<>();
        for (int i = 0; i < NO_OF_CLIENTS; i++) {
            Future<PerformanceData> pf = executorService.submit(new KVTestClient(TEST_DATA_DIRECTORY, PERCENTAGE_WRITES));
            list.add(pf);
        }
        for (Future<PerformanceData> pf : list) {
            try {
                pfList.add(pf.get());
            } catch (InterruptedException | ExecutionException e) {
                pfList.add(new PerformanceData());
            }
        }

        StringBuilder sb = new StringBuilder();

        System.out.println("Calculating performance measures.......................");
        sb.append("Performance Test Measures")
                .append(System.lineSeparator())
                .append("===================================================================")
                .append(System.lineSeparator())
                .append("No of parallel clients : " + NO_OF_CLIENTS)
                .append(System.lineSeparator())
                .append("No of rounds for each client : " + ROUNDS)
                .append(System.lineSeparator())
                .append(System.lineSeparator());


        List<PerformanceData.Entry> allData = pfList.stream().flatMap(it -> it.entries.stream()).collect(Collectors.toList());

        List<PerformanceData.Entry> getData = allData.stream().filter(it -> it.method.equals("GET")).collect(Collectors.toList());
        List<PerformanceData.Entry> getSuccessData = getData.stream().filter(it -> it.result.equals(KVMessage.StatusType.GET_SUCCESS.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> getNotFoundData = getData.stream().filter(it -> it.result.equals(KVMessage.StatusType.GET_NOT_FOUND.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> getErrorData = getData.stream().filter(it -> it.result.equals(KVMessage.StatusType.GET_ERROR.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> getServerNotFoundData = getData.stream().filter(it -> it.result.equals("SERVER_NOT_FOUND")).collect(Collectors.toList());
        List<PerformanceData.Entry> getExceptionsData = getData.stream().filter(it -> it.result.equals("ERROR")).collect(Collectors.toList());

        sb.append("Performance Measured on GET")
                .append(System.lineSeparator())
                .append("------------------------------------------------------")
                .append(System.lineSeparator())
                .append(String.format("Total %d GET requests executed with average response time %f", getData.size(), getData.size() > 0 ? getData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(String.format("Total %d GET_SUCCESS responses received with average response time %f", getSuccessData.size(), getSuccessData.size() > 0 ? getSuccessData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(String.format("Total %d GET_NOT_FOUND responses received with average response time %f", getNotFoundData.size(), getNotFoundData.size() > 0 ? getNotFoundData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(String.format("Total %d GET_ERROR responses received with average response time %f", getErrorData.size(), getErrorData.size() > 0 ? getErrorData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(String.format("Total %d GET_SERVER_NOT_FOUND responses received with average response time %f", getServerNotFoundData.size(), getServerNotFoundData.size() > 0 ? getServerNotFoundData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(String.format("Total %d GET_EXCEPTION responses received with average response time %f", getExceptionsData.size(), getExceptionsData.size() > 0 ? getExceptionsData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        List<PerformanceData.Entry> putData = allData.stream().filter(it -> it.method.equals("PUT")).collect(Collectors.toList());
        List<PerformanceData.Entry> putSuccessData = putData.stream().filter(it -> it.result.equals(KVMessage.StatusType.PUT_SUCCESS.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> putupdateData = putData.stream().filter(it -> it.result.equals(KVMessage.StatusType.PUT_UPDATE.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> putErrorData = putData.stream().filter(it -> it.result.equals(KVMessage.StatusType.PUT_ERROR.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> putServerNotFoundData = putData.stream().filter(it -> it.result.equals("SERVER_NOT_FOUND")).collect(Collectors.toList());
        List<PerformanceData.Entry> putExceptionsData = putData.stream().filter(it -> it.result.equals("ERROR")).collect(Collectors.toList());

        sb.append("Performance Measured on PUT")
                .append(System.lineSeparator())
                .append("------------------------------------------------------")
                .append(System.lineSeparator())
                .append(String.format("Total %d PUT requests executed with average response time %f", putData.size(), putData.size() > 0 ? putData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(String.format("Total %d PUT_SUCCESS responses received with average response time %f", putSuccessData.size(), putSuccessData.size() > 0 ? putSuccessData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(String.format("Total %d PUT_UPDATE responses received with average response time %f", putupdateData.size(), putupdateData.size() > 0 ? putupdateData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(String.format("Total %d PUT_ERROR responses received with average response time %f", putErrorData.size(), putErrorData.size() > 0 ? putErrorData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(String.format("Total %d PUT_SERVER_NOT_FOUND responses received with average response time %f", putServerNotFoundData.size(), putServerNotFoundData.size() > 0 ? putServerNotFoundData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(String.format("Total %d PUT_EXCEPTION responses received with average response time %f", putExceptionsData.size(), putExceptionsData.size() > 0 ? putExceptionsData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0))
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        try (FileWriter f = new FileWriter(STATS_FILE)){
            f.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Performance measures have been calculated successfully :)");
    }

    static {
        System.setProperty("log4j.configurationFile", "log4j2.properties.xml");
    }

}
