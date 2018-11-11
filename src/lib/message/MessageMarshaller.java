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
        if (message instanceof KVAdminMessage) {
            KVAdminMessage adminMessage = (KVAdminMessage) message;
            return marshallAdminMessage(adminMessage);
        }

        throw new MarshallingException("Unknown message type");
    }

    private static String marshallAdminMessage(KVAdminMessage adminMessage) {
        if (adminMessage.status == KVAdminMessage.StatusType.CONFIGURE)
            return String.join(RECORD_SEPARATOR,
                    adminMessage.status.name(),
                    adminMessage.meta.marshall()
            );

        if (adminMessage.status == KVAdminMessage.StatusType.MOVE)
            return String.join(RECORD_SEPARATOR,
                    adminMessage.status.name(),
                    adminMessage.metaContent.marshall()
                    );

        return adminMessage.status.name();
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


            return unmarshallKvMessage(kvMessageComponents);
        } catch (Exception e) {
            logger.warn("Exception while parsing message: '" + kvMessageString + "'", e);
            throw new MarshallingException(e);
        }
    }

    private static IMessage unmarshallKvMessage(String[] kvMessageComponents) {
        String key;
        String value;
        if (kvMessageComponents.length == 3) {
            key = !kvMessageComponents[1].equals("") ? kvMessageComponents[1] : null;
            value = !kvMessageComponents[2].equals("") ? kvMessageComponents[2] : null;
        } else if (kvMessageComponents.length == 2) {
            key = !kvMessageComponents[1].equals("") ? kvMessageComponents[1] : null;
            value = null;
        } else {
            key = null;
            value = null;
        }

        return new KVMessageImpl(key, value, KVMessage.StatusType.valueOf(kvMessageComponents[0]));
    }
}
