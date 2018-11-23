package ecs.command;

import ecs.Command;
import ecs.State;

/**
 * This class represents the command for adding a new server
 */
public class AddServerCommand implements Command {
    private String name;
    private final String ip;
    private final int port;

    /**
     * Constructor to create a new AddServerCommand
     *
     * @param name name of the server
     * @param ip ip of the server
     * @param port port of the server
     */
    public AddServerCommand(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    /**
     *Executes the command
     *
     * @param state state
     */
    @Override
    public void execute(State state) {

    }
}
