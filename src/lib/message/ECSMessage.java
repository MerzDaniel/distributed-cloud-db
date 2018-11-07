package lib.message;

public class ECSMessage implements KVAdminMessage{
    private final String host;
    private final int port;
    private final StatusType status;

    enum StatusType {
        INIT, 			        /* Initialize a KVServer */
        INIT_SUCCESS, 	        /* Initializing the KVServer is successfull */
        INIT_ERROR, 		    /* Initializing the KVServer is not successful */
        START, 			        /* Start a KVServer */
        START_SUCCESS, 	        /* Starting the KVServer is success */
        START_ERROR, 		    /* Starting the KVServer is not success  */
        STOP, 		            /* Stop a KVServer */
        STOP_SUCCESS,           /* Stoping the KVServer is success */
        STOP_ERROR, 	        /* Stoping the KVServer is not success */
        SHUT_DOWN, 		        /* Shut down a KVServer */
        SHUT_DOWN_SUCCESS,      /* Shutting down the KVServer is success */
        SHUT_DOWN_ERROR, 	    /* Shutting down the KVServer is not success */
        ADD, 		            /* Add a new KVServer */
        ADD_SUCCESS,            /* Adding the KVServer is success */
        ADD_ERROR, 	            /* Adding the KVServer is not success */
        REMOVE, 		        /* Remove a KVServer */
        REMOVE_SUCCESS,         /* Removing the KVServer is success */
        REMOVE_ERROR, 	        /* Removing down the KVServer is not success */
    }

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
