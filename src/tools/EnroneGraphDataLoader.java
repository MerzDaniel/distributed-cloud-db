package tools;


import lib.json.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.mutation.MutationMessageImpl;
import tools.util.EnroneBenchmarkDataLoader;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tools.Main.TEST_DATA_DIRECTORY;

public class EnroneGraphDataLoader {
    public static void Main() {
        Stream<AbstractMap.SimpleEntry<String, EnroneBenchmarkDataLoader.Loader>> dataStream =
                EnroneBenchmarkDataLoader.loadData(TEST_DATA_DIRECTORY, false);

        dataStream.limit(5)
                .map(v -> parseMessageFile(v.getValue().Load()))
                .map(msgs -> msgs.stream().map(m -> createMutations(m)))
                .flatMap(s -> s)
                .forEach(EnroneGraphDataLoader::print);

    }

    private static void print(MutationMessageImpl m) {
        try {
            System.out.println(m.marshall());
        } catch (MarshallingException e) {
            System.out.println(e.getMessage());
        }
    }

    private static MutationMessageImpl createMutations(Message m) {
//        MutationMessageImpl.Builder.create(
//        Json.Builder.create()
//                .withJsonProperty(m.id)
        return null;
    }

    private static List<Message> parseMessageFile(String fileContent) {
        String[] split = fileContent.split("\\r?\\n", 10);
        Message msg = new Message();
        int index = 0;
        msg.id = split[index++];
        msg.date = split[index++].substring(6);
        msg.fromUser = split[index++].substring(6);
        msg.fromUser = split[index++].substring(6);
        msg.toUser = parseUsersFromLine(split[index++]);
        while(!split[index].startsWith("Subject:")) {
            msg.toUser.addAll(parseUsersFromLine(split[index++]));
        }
        msg.subject = split[index++].substring(6);

        return Arrays.asList(msg);
    }

    private static List<String> parseUsersFromLine(String line) {
        return Arrays.stream(
                line.substring(6).split(",")
        ).map(s -> s.trim()).collect(Collectors.toList());
    }

    private static class Message{
        String id;
        String date;
        String subject;
        String fromUser;
        List<String> toUser;

    }
}
