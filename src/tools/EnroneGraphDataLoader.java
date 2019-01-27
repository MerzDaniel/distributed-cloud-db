package tools;

import lib.message.Messaging;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.graph.response.ResponseMessageImpl;
import org.jetbrains.annotations.NotNull;
import tools.util.EnroneBenchmarkDataLoader;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EnroneGraphDataLoader {

    /*
    user: <id-is-email>: {
        id: <email>,
        messages: [<messagesIds>],
        messagesReceived: [<messageIds>]
    }

    message: <message-id>: {
        id: <message-id>,
        date: <date>,
        subject: <txt>,
        fromUser: <user-id>,
        toUsers: [<user-ids>],
    }

     */

    public static void main(@NotNull String[] args) throws IOException {
        if (args.length != 1) System.out.println("loader.jar <path-to-enrone-test-data-set>");

        int index = 0;
        int dataLimit = 5;
        boolean writeToServer = false;
        File dataDirectory = new File(args[index++]);


        Stream<AbstractMap.SimpleEntry<String, EnroneBenchmarkDataLoader.Loader>> dataStream =
                EnroneBenchmarkDataLoader.loadData(dataDirectory, true);

        Stream<MutationMessageImpl> mutationMessageStream = dataStream.limit(dataLimit)
                .map(v -> parseMessageFile(v.getKey(), v.getValue().Load()))
                .map(msgs -> msgs.stream().map(m -> createMutations(m)))
                .flatMap(s -> s);

        if (!writeToServer) {
            mutationMessageStream.forEach(EnroneGraphDataLoader::print);
            return;
        }

        Messaging messaging = new Messaging("localhost", 50000);
        mutationMessageStream.map(m -> {
            try {
                messaging.sendMessage(m);
                return ((ResponseMessageImpl)messaging.readMessage()).errorMsg;
            } catch (Exception e) {
                return e.getMessage();
            }
        }).forEach(System.out::println);
    }

    private static void print(MutationMessageImpl m) {
        System.out.println(m.prettyPrint());
    }

    private static MutationMessageImpl createMutations(Message m) {

        MutationMessageImpl.Builder builder = MutationMessageImpl.Builder.create();
        builder
                // message
                .withReplace(m.id, "id", m.id)
                .withReplace(m.id, "date", m.date)
                .withReplace(m.id, "fromUser", m.fromUser)
                .withReplace(m.id, "subject", m.subject)
                .withReplace(m.id, "type", "message")
                .withMerge(m.id, "toUsers", m.toUsers)
                // from user
                .withReplace(m.fromUser, "id", m.fromUser)
                .withReplace(m.fromUser, "type", "user")
                .withStringArrayMerge(m.fromUser, "messages", Arrays.asList(m.id))
        ;
        // to users
        m.toUsers.stream().forEach(u ->
                builder
                        .withReplace(u, "id", u)
                        .withReplace(u, "type", "user")
                        .withStringArrayMerge(u, "messagesReceived", Arrays.asList(m.id))
        );

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
