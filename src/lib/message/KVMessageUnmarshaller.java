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
            String key;
            String value;

            if (kvMessageComponents.length == 3){
                key = kvMessageComponents[1] != "" ? kvMessageComponents[1] : null;
                value = kvMessageComponents[2] != "" ? kvMessageComponents[2] : null;
            }
            else if(kvMessageComponents.length == 2){
                key = kvMessageComponents[1] != "" ? kvMessageComponents[1] : null;
                value = null;
            }
            else {
                key = null;
                value = null;
            }

            return new KVMessageImpl(key, value, KVMessage.StatusType.valueOf(kvMessageComponents[0]));
        } catch (Exception e) {
            logger.warn("Exception while parsing message: '" + kvMessageString + "'", e);
            throw new UnmarshallException(e);
        }
    }
}
