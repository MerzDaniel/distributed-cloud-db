package ecs.command;

import ecs.Command;
import ecs.State;

public class AddServerCommand implements Command {
    private String name;
    private final String ip;
    private final int port;

    public AddServerCommand(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }
    @Override
    public void execute(State state) {

    }
}
