package tools;

import lib.message.graph.mutation.MutationMessageImpl;
import org.jetbrains.annotations.NotNull;
import tools.util.EnroneBenchmarkDataLoader;

import java.io.File;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EnroneGraphDataLoader {

    public static void main(@NotNull String[] args) {
        if (args.length != 1) System.out.println("loader.jar <path-to-enrone-test-data-set>");

        File dataDirectory = new File(args[0]);

        Stream<AbstractMap.SimpleEntry<String, EnroneBenchmarkDataLoader.Loader>> dataStream =
                EnroneBenchmarkDataLoader.loadData(dataDirectory, true);

        dataStream.limit(5)
                .map(v -> parseMessageFile(v.getKey(), v.getValue().Load()))
                .map(msgs -> msgs.stream().map(m -> createMutations(m)))
                .flatMap(s -> s)
                .forEach(EnroneGraphDataLoader::print);

    }

    private static void print(MutationMessageImpl m) {
        System.out.println(m.prettyPrint());
    }

    private static MutationMessageImpl createMutations(Message m) {

        MutationMessageImpl.Builder builder = MutationMessageImpl.Builder.create();
        builder
                .withReplace(m.id, "id", m.id)
                .withReplace(m.id, "date", m.date)
                .withReplace(m.id, "fromUser", m.fromUser)
                .withReplace(m.id, "subject", m.subject)
                .withMerge(m.id, "toUsers", m.toUsers);
        return builder.finish();
    }

    private static List<Message> parseMessageFile(String key, String fileContent) {
        try {
            String[] split = fileContent.split("\\r?\\n", 10);
            Message msg = new Message();
            int index = 0;
            msg.id = split[index++].substring(12);
            msg.date = split[index++].substring(6);
            msg.fromUser = split[index++].substring(6);
            msg.toUsers = parseUsersFromLine(split[index++]);
            while (!split[index].startsWith("Subject:")) {
                msg.toUsers.addAll(parseUsersFromLine(split[index++]));
            }
            msg.subject = split[index++].substring(9);

            return Arrays.asList(msg);
        } catch (Exception e) {
            System.out.println(e.getMessage());

            System.out.println(key);
            return Collections.emptyList();
        }
    }

    private static List<String> parseUsersFromLine(String line) {
        return Arrays.stream(
                line.substring(4).split(",")
        ).map(s -> s.trim()).collect(Collectors.toList());
    }

    private static class Message {
        String id;
        String date;
        String subject;
        String fromUser;
        List<String> toUsers;

    }
}
