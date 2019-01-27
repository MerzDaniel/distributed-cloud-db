package client.ui.commands;

import client.ui.ApplicationState;
import client.ui.Command;
import lib.TimeWatch;
import lib.json.Json;
import lib.message.exception.MarshallingException;
import lib.message.graph.mutation.MutationMessageImpl;
import lib.message.graph.response.ResponseMessageImpl;
import lib.message.kv.KVMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Issues a Mutate command on the server
 */
public class MutationCommand implements Command {
    private String queryParam;
    private String query;

    private final Logger logger = LogManager.getLogger(MutationCommand.class);

    public MutationCommand(String queryParam, String query) {
        this.queryParam = queryParam;
        this.query = query;
    }

    @Override
    public void execute(ApplicationState state) {

        ResponseMessageImpl graphMessageResponse = null;
        TimeWatch t = TimeWatch.start();
        MutationMessageImpl mutationMessage = null;
        try {
            mutationMessage = constructMutationMsg();
            System.out.println(String.format("Mutate >> : '%s'", mutationMessage.prettyPrint()));
            graphMessageResponse = state.kvStore.mutate(mutationMessage);
        } catch (IOException e) {
            logger.error("error", e);
            System.out.println(String.format("An Error occurred during the MUATE : %s (%d ms)", e.getMessage(), t.time()));
            return;
        } catch (MarshallingException e) {
            logger.error("Error during unmarshalling.", e);
            System.out.println("Response from the server was invalid.");
            return;
        }

        if (graphMessageResponse.errorMsg.equals(KVMessage.StatusType.SERVER_STOPPED.name())) {
            logger.info(KVMessage.StatusType.SERVER_STOPPED + String.format("The server is stopped so cannot perform the request query<%s>", mutationMessage.prettyPrint()));
            System.out.println("The server is stopped so cannot perform the request");
            return;
        }

        if (graphMessageResponse.errorMsg.equals(KVMessage.StatusType.SERVER_WRITE_LOCK.name())) {
            logger.info(KVMessage.StatusType.SERVER_WRITE_LOCK + String.format("The server is locked for writing so cannot perform the request query<%s>", mutationMessage.prettyPrint()));
            System.out.println("The server is locked for writing. Please try again later");
            return;
        }

        if (graphMessageResponse.errorMsg != null && !graphMessageResponse.errorMsg.equals(""))
            System.out.println("Errors occured: " + graphMessageResponse.errorMsg);
        else
            System.out.println(String.format("Data saved successfully >>: '%s' (%d ms)", graphMessageResponse.data.prettyPrint(), t.time()));
    }

    @Override
    public String toString() {
        return "MUTATE<" + queryParam + " " + query + ">";
    }

    private MutationMessageImpl constructMutationMsg(){
        MutationMessageImpl.Builder mutationMessageBuilder = MutationMessageImpl.Builder.create();

        String[] querySplits = query.split(",");

        for(String querySplit : querySplits){
            String[] keyValueSplits = querySplit.split(":");
            String key = keyValueSplits[0];
            String value = keyValueSplits[1];
            mutationMessageBuilder.withReplace(queryParam, key, new Json.StringValue(value));
        }

        return mutationMessageBuilder.finish();
    }
}
