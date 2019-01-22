package lib.message.KvMessage;

import lib.message.IMessage;

/**
 * Represents the abstract structure of the message passed between client and the server
 */
public interface KVMessage extends IMessage {

    public enum StatusType {
        GET, 			        /* Get - request */
        GET_NOT_FOUND, 	        /* requested tuple (i.e. value) not found */
        GET_ERROR, 		        /* generic Get error */
        GET_SUCCESS, 	        /* requested tuple (i.e. value) found */
        PUT, 			        /* Put - request */
        PUT_SUCCESS, 	        /* Put - request successful, tuple inserted */
        PUT_UPDATE, 	        /* Put - request successful, i.e. value updated */
        PUT_ERROR, 		        /* Put - request not successful */
        DELETE, 		        /* Delete - request */
        DELETE_SUCCESS,         /* Delete - request successful */
        DELETE_ERROR, 	        /* Delete - request successful */
        INVALID_MESSAGE,        /* Invalid message */
        CONNECT_SUCCESSFUL,     /* Connection successful message */
        CONNECT_ERROR,          /* Connect error message */
        SERVER_STOPPED,         /* Server is stopped, no requests are processed */
        SERVER_WRITE_LOCK,      /* Server locked for out, only get possible */
        SERVER_NOT_RESPONSIBLE, /* Request not successful, server not responsible for key */
        SERVER_NOT_FOUND,       /* Couldn't find the server responsible for the request */
        SERVER_ERROR            /* General server errors */
    }

    /**
     * @return the key that is associated with this message,
     * null if not key is associated.
     */
    public String getKey();

    /**
     * @return the value that is associated with this message,
     * null if not value is associated.
     */
    public String getValue();

    /**
     * @return a status string that is used to identify request types,
     * response types and error types associated to the message.
     */
    public StatusType getStatus();

    /**
     *
     * @return a boolean if this message has an error status
     */
    public boolean isError();


    /**
     *
     * @return a boolean if this message has an success status
     */
    public boolean isSuccess();

}
