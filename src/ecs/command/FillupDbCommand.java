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
        KVStore store = new KVStore(state.storeMeta);
        Random r = new Random();
        Exception ex = null;
        for (int i = 0; i < amount; i++) {
            try {
                String key = ("randomKey-" + r.nextInt());
                KVMessage put = store.put(key.substring(0,Math.min(key.length()-1, 19)), "randomValue-" + r.nextInt());
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
