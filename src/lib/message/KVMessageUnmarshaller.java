package lib.message;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provied the methods to unmarshall a string to a {@link KVMessage} instance
 */
public class KVMessageUnmarshaller {
    static Logger logger = LogManager.getLogger(KVMessageUnmarshaller.class);
    private final static String RECORD_SEPARATOR = "\u001E";

    private KVMessageUnmarshaller(){}

    /**
     * Returns KVMessage by unmarshalling the {@code kvMessageString}
     *
     * @param kvMessageString the string to be unmarshalled
     * @return a {@link KVMessage} by unmarshalling the {@code kvMessageString}
     * @throws UnmarshallException if the given {@code kvMessageString} cannot be unmarshalled
     */
    public static KVMessage unmarshall(String kvMessageString) throws UnmarshallException{
        try {
            String[] kvMessageComponents = kvMessageString.split(RECORD_SEPARATOR);
            return new KVMessageImpl(kvMessageComponents[1], kvMessageComponents[2], KVMessage.StatusType.valueOf(kvMessageComponents[0]));
        } catch (Exception e) {
            logger.warn("Exception while parsing message: '" + kvMessageString + "'", e);
            throw new UnmarshallException(e);
        }
    }
}
