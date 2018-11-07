package lib.message;

/**
 * This class provied the methods to marshall a {@link KVMessage} instance
 */
public class KVMessageMarshaller {

    private final static String RECORD_SEPARATOR = "\u001E";
    private KVMessageMarshaller(){}

    /**
     * Returns marshalled string representation of the {@code kvMessage}
     *
     * @param kvMessage the object to be marshalled
     * @return a string representation of the {@code kvMessage}
     */
    public static String marshall(KVMessage kvMessage){
        return kvMessage.getStatus().name()
                + RECORD_SEPARATOR
                + kvMessage.getKey()
                + RECORD_SEPARATOR
                + kvMessage.getValue();
    }
}
