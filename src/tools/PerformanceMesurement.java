package tools;

import lib.message.kv.KVMessage;
import tools.util.PerformanceData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This class is only used for getting performance measures for KVClient
 */
public class PerformanceMesurement {
    static {
        System.setProperty("log4j.configurationFile", "log4j2-tools.properties.xml");
    }

    static int ROUNDS;
    static int NO_OF_CLIENTS;
    final static double PERCENTAGE_WRITES = 0.2;

    static ExecutorService executorService;

    final static File STATS_DIRECTORY = new File(Paths.get("stats").toUri());
    final static File STATS_TXT_FILE = new File(Paths.get(STATS_DIRECTORY.toString(), "stats.txt").toUri());
    final static File STATS_FILE = new File(Paths.get(STATS_DIRECTORY.toString(), "stats.csv").toUri());

    static File TEST_DATA_DIRECTORY;

    public static void main(String[] args) {
        TEST_DATA_DIRECTORY = new File(args[0]);
        NO_OF_CLIENTS = Integer.parseInt(args[1]);
        ROUNDS = Integer.parseInt(args[2]);
        executorService = Executors.newFixedThreadPool(NO_OF_CLIENTS);

        if (!STATS_DIRECTORY.exists()) STATS_DIRECTORY.mkdir();

        runTest();
        executorService.shutdown();
    }

    public static void runTest() {
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

        List<Double> percentiles = Arrays.asList(0.99, 0.95, 0.90, 0.80, 0.5);
        long[] sizePerTime = pfList.stream().collect(Collector.of(
                () -> new long[2],
                (acc, perf) -> {
                    acc[0] += perf.entries.size();
                    acc[1] += perf.elapsedTime;
                },
                (acc1, acc2) -> {
                    acc1[0] += acc2[0];
                    acc1[1] += acc2[1];
                    return acc1;
                },
                acc -> acc
        ));
        List<PerformanceData.Entry> allData = pfList.stream().flatMap(it -> it.entries.stream()).collect(Collectors.toList());
        List<Long> allPercentiles = calculatePercentiles(allData, percentiles);


        System.out.println("Calculating performance measures.......................");
        StringBuilder sb = new StringBuilder();
        sb.append("Performance Test Measures")
                .append(System.lineSeparator())
                .append("===================================================================")
                .append(System.lineSeparator())
                .append("No of parallel clients : " + NO_OF_CLIENTS)
                .append(System.lineSeparator())
                .append("No of rounds for each client : " + ROUNDS)
                .append(System.lineSeparator())
                .append(String.format("Percentiles of ALL requests: %s : %s", percentiles, allPercentiles))
                .append(System.lineSeparator());


        List<PerformanceData.Entry> getData = allData.stream().filter(it -> it.method.equals("GET")).collect(Collectors.toList());
        List<PerformanceData.Entry> getSuccessData = getData.stream().filter(it -> it.result.equals(KVMessage.StatusType.GET_SUCCESS.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> getNotFoundData = getData.stream().filter(it -> it.result.equals(KVMessage.StatusType.GET_NOT_FOUND.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> getErrorData = getData.stream().filter(it -> it.result.equals(KVMessage.StatusType.GET_ERROR.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> getServerNotFoundData = getData.stream().filter(it -> it.result.equals("SERVER_NOT_FOUND")).collect(Collectors.toList());
        List<PerformanceData.Entry> getExceptionsData = getData.stream().filter(it -> it.result.equals("ERROR")).collect(Collectors.toList());

        sb.append("Performance Measured on GET")
                .append(System.lineSeparator())
                .append("------------------------------------------------------\n")
                .append(String.format("Total %d GET requests executed with average response time %f %s\n",
                        getData.size(), getData.size() > 0 ? getData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(getData, percentiles)))
                .append(String.format("Total %d GET_SUCCESS responses received with average response time %f %s\n",
                        getSuccessData.size(), getSuccessData.size() > 0 ? getSuccessData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(getSuccessData, percentiles)))
                .append(String.format("Total %d GET_NOT_FOUND responses received with average response time %f %s\n",
                        getNotFoundData.size(), getNotFoundData.size() > 0 ? getNotFoundData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(getNotFoundData, percentiles)))
                .append(String.format("Total %d GET_ERROR responses received with average response time %f %s\n",
                        getErrorData.size(), getErrorData.size() > 0 ? getErrorData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(getErrorData, percentiles)))
                .append(String.format("Total %d GET_SERVER_NOT_FOUND responses received with average response time %f %s\n",
                        getServerNotFoundData.size(), getServerNotFoundData.size() > 0 ? getServerNotFoundData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(getServerNotFoundData, percentiles)))
                .append(String.format("Total %d GET_EXCEPTION responses received with average response time %f %s\n",
                        getExceptionsData.size(), getExceptionsData.size() > 0 ? getExceptionsData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(getExceptionsData, percentiles)));

        List<PerformanceData.Entry> putData = allData.stream().filter(it -> it.method.equals("PUT")).collect(Collectors.toList());
        List<PerformanceData.Entry> putSuccessData = putData.stream().filter(it -> it.result.equals(KVMessage.StatusType.PUT_SUCCESS.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> putupdateData = putData.stream().filter(it -> it.result.equals(KVMessage.StatusType.PUT_UPDATE.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> putErrorData = putData.stream().filter(it -> it.result.equals(KVMessage.StatusType.PUT_ERROR.name())).collect(Collectors.toList());
        List<PerformanceData.Entry> putServerNotFoundData = putData.stream().filter(it -> it.result.equals("SERVER_NOT_FOUND")).collect(Collectors.toList());
        List<PerformanceData.Entry> putExceptionsData = putData.stream().filter(it -> it.result.equals("ERROR")).collect(Collectors.toList());

        sb.append("Performance Measured on PUT")
                .append(System.lineSeparator())
                .append("------------------------------------------------------")
                .append(String.format("\nTotal %d PUT requests executed with average response time %f %s\n",
                        putData.size(), putData.size() > 0 ? putData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(putData, percentiles)))
                .append(String.format("Total %d PUT_SUCCESS responses received with average response time %f %s\n",
                        putSuccessData.size(), putSuccessData.size() > 0 ? putSuccessData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(putSuccessData, percentiles)))
                .append(String.format("Total %d PUT_UPDATE responses received with average response time %f %s\n",
                        putupdateData.size(), putupdateData.size() > 0 ? putupdateData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(putupdateData, percentiles)))
                .append(String.format("Total %d PUT_ERROR responses received with average response time %f %s\n",
                        putErrorData.size(), putErrorData.size() > 0 ? putErrorData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(putErrorData, percentiles)))
                .append(String.format("Total %d PUT_SERVER_NOT_FOUND responses received with average response time %f %s\n",
                        putServerNotFoundData.size(), putServerNotFoundData.size() > 0 ? putServerNotFoundData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(putServerNotFoundData, percentiles)))
                .append(String.format("Total %d PUT_EXCEPTION responses received with average response time %f %s\n",
                        putExceptionsData.size(), putExceptionsData.size() > 0 ? putExceptionsData.stream().mapToDouble(it -> it.time).average().getAsDouble() : 0.0,
                        calculatePercentiles(putExceptionsData, percentiles)))
                .append(System.lineSeparator());

        try (FileWriter f = new FileWriter(STATS_TXT_FILE)) {
            f.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Performance measures have been calculated successfully :)");
    }

    static List<Long> calculatePercentiles(List<PerformanceData.Entry> perfData, List<Double> percentils) {
        List<Long> result = new LinkedList<>();

        if (perfData.isEmpty()) return result;
        perfData.sort((p1, p2) -> {
            if (p1.time < p2.time)
                return 1;
            if (p1.time == p2.time)
                return 0;
            return -1;
        });
        percentils.forEach(p ->
                result.add(perfData.stream().skip((long) (perfData.size() * (1 - (p)) - 1)).findAny().get().time)
        );
        return result;
    }


}
