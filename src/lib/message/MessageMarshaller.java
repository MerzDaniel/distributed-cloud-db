package lib.message;

import lib.metadata.KVStoreMetaData;
import lib.metadata.ServerData;
import lib.server.RunningState;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
                    adminMessage.meta.marshall(),
                    String.valueOf(adminMessage.currentServerIndex)
            );

        if (adminMessage.status == KVAdminMessage.StatusType.MOVE)
            return String.join(RECORD_SEPARATOR,
                    adminMessage.status.name(),
                    adminMessage.serverData.marshall()
            );

        if (adminMessage.status == KVAdminMessage.StatusType.MOVE_SOFT)
            return String.join(RECORD_SEPARATOR,
                    adminMessage.status.name(),
                    adminMessage.serverData.marshall()
            );

        if (adminMessage.status == KVAdminMessage.StatusType.STATUS_RESPONSE)
            return String.join(RECORD_SEPARATOR,
                    adminMessage.status.name(),
                    adminMessage.runningState.name()
            );

        if (adminMessage.status == KVAdminMessage.StatusType.DATA_MOVE)
            return String.join(RECORD_SEPARATOR,
                    adminMessage.status.name(),
                    adminMessage.key,
                    adminMessage.value
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
            String[] kvMessageComponents = kvMessageString.split(RECORD_SEPARATOR);
            String key;
            String value;

            if (isKvMessage(kvMessageComponents[0]))
                return unmarshallKvMessage(kvMessageComponents);
            if (isAdminMessage(kvMessageComponents[0]))
                return unmarshallKvAdminMessage(kvMessageComponents);

            throw new Exception("Unknown status type");
        } catch (Exception e) {
            logger.warn("Exception while parsing message: '" + kvMessageString + "'", e);
            throw new MarshallingException(e);
        }
    }

    private static IMessage unmarshallKvAdminMessage(String[] kvMessageComponents) throws MarshallingException {
        KVAdminMessage.StatusType status = KVAdminMessage.StatusType.valueOf(kvMessageComponents[0]);

        String secondPart;

        switch (status) {
            case CONFIGURE:
                secondPart = Arrays.stream(kvMessageComponents).limit(kvMessageComponents.length - 1).skip(1).collect(Collectors.joining(RECORD_SEPARATOR));
                return new KVAdminMessage(
                        status, KVStoreMetaData.unmarshall(secondPart), Integer.valueOf(kvMessageComponents[kvMessageComponents.length - 1])
                );

            case MOVE:
                secondPart = Arrays.stream(kvMessageComponents).skip(1).collect(Collectors.joining(RECORD_SEPARATOR));
                return new KVAdminMessage(status, ServerData.unmarshall(secondPart));

            case MOVE_SOFT:
                secondPart = Arrays.stream(kvMessageComponents).skip(1).collect(Collectors.joining(RECORD_SEPARATOR));
                return new KVAdminMessage(status, ServerData.unmarshall(secondPart));

            case STATUS_RESPONSE:
                secondPart = Arrays.stream(kvMessageComponents).skip(1).collect(Collectors.joining(RECORD_SEPARATOR));
                return new KVAdminMessage(status, RunningState.valueOf(secondPart));

            case DATA_MOVE:
                return new KVAdminMessage(status, kvMessageComponents[1], kvMessageComponents[2]);

            default:
                return new KVAdminMessage(status);
        }
    }

    private static boolean isKvMessage(String status) {
        try {
            KVMessage.StatusType.valueOf(status);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private static boolean isAdminMessage(String status) {
        try {
            KVAdminMessage.StatusType.valueOf(status);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
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
        } else if (kvMessageComponents.length > 3){
            key = !kvMessageComponents[1].equals("") ? kvMessageComponents[1] : null;
            List<String> kvMsgCompList = Arrays.asList(kvMessageComponents);
            value = kvMsgCompList.subList(2, kvMsgCompList.size()).stream().collect(Collectors.joining(RECORD_SEPARATOR));
        } else {
            key = null;
            value = null;
        }

        return new KVMessageImpl(key, value, KVMessage.StatusType.valueOf(kvMessageComponents[0]));
    }
}
