package ecs.command;

import ecs.Command;
import ecs.State;

/**
 * This class represent the command for printing the usage of other {@link Command} instances
 */
public class UsageCommand implements Command {

    /**
     * Execute the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {
        System.out.println("Usage:");
        System.out.println("  Add <name> <host> <port> : Add a new Storage server. It will be added to the config.");
        System.out.println("  check                    : Check state of servers");
        System.out.println("  start                    : Start the storage servers");
        System.out.println("  stop <name>              : Stop a server");
        System.out.println("  help                     : Show this help");
    }
}
