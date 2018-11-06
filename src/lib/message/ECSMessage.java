package lib.message;

public class ECSMessage implements KVAdminMessage{
    private final String host;
    private final int port;
    private final StatusType status;

    /**
     * Create a ECSMesage
     *
     * @param host host of the message
     * @param port port of the message
     * @param status status of the message
     */
    public ECSMessage(String host, int port, StatusType status) {
        this.host = host;
        this.port = port;
        this.status = status;
    }

    /**
     *
     * @return the statusType associated with this message
     */
    @Override
    public StatusType getStatus() {
        return this.status;
    }

    /**
     *
     * @return the host associated with this message
     */
    @Override
    public String getHost() {
        return this.host;
    }

    /**
     *
     * @return the port associated with this message
     */
    @Override
    public int getPort() {
        return this.port;
    }
}
