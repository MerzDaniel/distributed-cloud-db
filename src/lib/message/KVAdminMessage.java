package lib.message;

/**
 * This is the absruct structure for the messages between KVServer and ECS
 */
public interface KVAdminMessage {

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
     *
     * @return the statusType associated with this message
     */
    public StatusType getStatus();

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
