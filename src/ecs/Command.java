package ecs;

/**
 * A command which can be executed. Commands may write messages to the user using System.out
 */
public interface Command {
    void execute(State state);
}
