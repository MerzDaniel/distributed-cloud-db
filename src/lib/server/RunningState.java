package lib.server;

public enum RunningState {
    /** No meta configured */
    UNCONFIGURED,
    /** Configured but does not handle client requests */
    IDLE,
    /** Handles client READ requests */
    READONLY,
    /** Normally handle all requests */
    RUNNING,
    /** Server is shutting down will not be reachable in a few seconds */
    SHUTTINGDOWN,
}
