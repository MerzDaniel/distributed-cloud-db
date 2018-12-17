package ecs.command;

import client.store.KVStore;
import ecs.Command;
import ecs.State;
import lib.message.KVMessage;

import java.util.Random;

public class FillupDbCommand implements Command {
    private int amount;

    public FillupDbCommand(int amount) {
        this.amount = amount;
    }

    @Override
    public void execute(State state) {
        KVStore store = new KVStore(state.poolMeta);
        Random r = new Random();
        Exception ex = null;
        for (int i = 0; i < amount; i++) {
            try {
                KVMessage put = store.put("randomKey-" + r.nextInt(), "randomValue-" + r.nextInt());
                if (put.getStatus() != KVMessage.StatusType.PUT_SUCCESS) ex = new Exception(put.getStatus().name());

            } catch (Exception e) {
                ex = e;
            }
        }
        if (ex != null) {
            System.out.println("Errors occured");
            System.out.println(ex.getMessage());
        }
        System.out.println("Done");
    }
}
