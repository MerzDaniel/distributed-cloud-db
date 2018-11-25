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
        System.out.println("  Add <name> <host> <port> : [NOT IMPLEMENTED] Add a new Storage server. It will be added to the config.");
        System.out.println("  status                   : Check state of servers");
        System.out.println("  startup                  : Startup KvServer processes ( -> UNCONFIGURED state)");
        System.out.println("  configure                : Configure KvServers ( UNCONFIGURE -> IDLE )");
        System.out.println("  start                    : Start the storage servers ( IDLE -> RUNNING )");
        System.out.println("  stop                     : Stop a all servers ( RUNNING -> IDLE )");
        System.out.println("  shutdown                 : Stop a all server processes");
        System.out.println("  help                     : Show this help");
    }
}
