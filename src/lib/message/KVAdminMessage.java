package lib.message;

/**
 * This is the absruct structure for the messages between KVServer and ECS
 */
public interface KVAdminMessage {

    /**
     *
     * @return the host associated with this message
     */
    public String getHost();

    /**
     *
     * @return the port associated with this message
     */
    public int getPort();
}
