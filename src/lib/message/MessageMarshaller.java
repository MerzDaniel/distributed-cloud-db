package lib.message;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * This class provied the methods to marshall a {@link KVMessage} instance
 */
public final class MessageMarshaller {

    static Logger logger = LogManager.getLogger(MessageMarshaller.class);
    private final static String RECORD_SEPARATOR = "\u001E";

    /**
     * Returns marshalled string representation of the {@code kvMessage}
     *
     * @param message the object to be marshalled
     * @return a string representation of the {@code kvMessage}
     */
    public static String marshall(IMessage message) throws MarshallingException {
        if (message instanceof KVMessage) {
            KVMessage kvMessage = (KVMessage) message;
            return kvMessage.getStatus().name()
                    + RECORD_SEPARATOR
                    + (kvMessage.getKey() != null ? kvMessage.getKey() : "")
                    + RECORD_SEPARATOR
                    + (kvMessage.getValue() != null ? kvMessage.getValue() : "");
        }

        throw new MarshallingException("Unknown message type");
    }

    /**
     * Returns KVMessage by unmarshalling the {@code kvMessageString}
     *
     * @param kvMessageString the string to be unmarshalled
     * @return a {@link KVMessage} by unmarshalling the {@code kvMessageString}
     * @throws MarshallingException if the given {@code kvMessageString} cannot be unmarshalled
     */
    public static IMessage unmarshall(String kvMessageString) throws MarshallingException {
        try {
            String[] kvMessageComponents = kvMessageString.split(RECORD_SEPARATOR, 3);
            String key;
            String value;

            if (kvMessageComponents.length == 3){
                key = !kvMessageComponents[1].equals("") ? kvMessageComponents[1] : null;
                value = !kvMessageComponents[2].equals("") ? kvMessageComponents[2] : null;
            }
            else if(kvMessageComponents.length == 2){
                key = !kvMessageComponents[1].equals("") ? kvMessageComponents[1] : null;
                value = null;
            }
            else {
                key = null;
                value = null;
            }

            return new KVMessageImpl(key, value, KVMessage.StatusType.valueOf(kvMessageComponents[0]));
        } catch (Exception e) {
            logger.warn("Exception while parsing message: '" + kvMessageString + "'", e);
            throw new MarshallingException(e);
        }
    }
}
