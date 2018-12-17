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
        System.out.println("  init <no-of-servers> <cache-size> <cache-type>");
        System.out.println("                           : initialize the KVStore");
        System.out.println("  add  <cache-size> <cache-type>");
        System.out.println("                           : Add a new  KVServer");
        System.out.println("  remove                   : Remove a KVServer");
        System.out.println("  status                   : Check state of KVServers");
        System.out.println("  gstatus                  : Check state of servers using gossip infrastructure (only one request)");
        System.out.println("  startup                  : Startup KVServer processes ( -> UNCONFIGURED state)");
        System.out.println("  configure                : Configure KVServers ( UNCONFIGURE -> IDLE )");
        System.out.println("  start                    : Start the KVServers ( IDLE -> RUNNING )");
        System.out.println("  stop                     : Stop all KVServers ( RUNNING -> IDLE )");
        System.out.println("  failover                 : if one node failed, it will remove the node and data will be replicated to other servers");
        System.out.println("  shutdown                 : Shutdown all KVServer processes");
        System.out.println("  help                     : Show this help");
    }
}
